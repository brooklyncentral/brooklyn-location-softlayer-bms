/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brooklyn.location.softlayer.bms;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import brooklyn.location.LocationSpec;
import brooklyn.location.MachineProvisioningLocation;
import brooklyn.location.NoMachinesAvailableException;
import brooklyn.location.basic.LocationConfigKeys;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.location.cloud.AbstractCloudMachineProvisioningLocation;
import brooklyn.location.cloud.CloudMachineNamer;
import brooklyn.location.jclouds.JcloudsLocationConfig;
import brooklyn.location.jclouds.JcloudsMachineNamer;
import brooklyn.location.softlayer.bms.client.SoftLayerBmsClient;
import brooklyn.location.softlayer.bms.domain.BareMetalOrderSupplier;
import brooklyn.location.softlayer.bms.domain.Order;
import brooklyn.location.softlayer.bms.domain.Price;
import brooklyn.management.AccessController;
import brooklyn.util.collections.Jsonya;
import brooklyn.util.config.ConfigBag;
import brooklyn.util.exceptions.CompoundRuntimeException;
import brooklyn.util.exceptions.Exceptions;
import brooklyn.util.flags.SetFromFlag;
import brooklyn.util.javalang.Reflections;
import brooklyn.util.repeat.Repeater;
import brooklyn.util.text.Strings;
import brooklyn.util.time.Duration;
import brooklyn.util.time.Time;

public class SoftlayerBmsLocation extends AbstractCloudMachineProvisioningLocation implements JcloudsLocationConfig, MachineProvisioningLocation<SshMachineLocation> {

    private static final Logger LOG = LoggerFactory.getLogger(SoftlayerBmsLocation.class);
    private static final String ACTIVE = "ACTIVE";

    private SoftLayerBmsClient softLayerBmsClient;

    @SetFromFlag
    private final Map<SshMachineLocation, String> vmInstanceIds = Maps.newLinkedHashMap();

    @Override
    public void init() {
        super.init();
        ConfigBag setup = getAllConfigBag();
        String identity = checkNotNull(setup.get(ACCESS_IDENTITY), "identity must not be null");
        String credential = checkNotNull(setup.get(ACCESS_CREDENTIAL), "credential must not be null");
        softLayerBmsClient = new SoftLayerBmsClient(identity, credential);
    }

    public SoftLayerBmsClient getSoftLayerBmsClient() {
        return softLayerBmsClient;
    }

    public Map<SshMachineLocation, String> getVmInstanceIds() {
        return vmInstanceIds;
    }

    @Override
    public SshMachineLocation obtain(Map<?, ?> flags) throws NoMachinesAvailableException {

        ConfigBag setup = ConfigBag.newInstanceExtending(getAllConfigBag(), flags);
        Integer attempts = setup.get(MACHINE_CREATE_ATTEMPTS);
        List<Exception> exceptions = Lists.newArrayList();
        if (attempts == null || attempts < 1) attempts = 1;
        for (int i = 1; i <= attempts; i++) {
            try {
                return obtainOnce(setup);
            } catch (RuntimeException e) {
                LOG.warn("Attempt #{}/{} to obtain machine threw error: {}", new Object[]{i, attempts, e});
                exceptions.add(e);
            }
        }
        String msg = format("Failed to get VM after %d attempt%s.", attempts, attempts == 1 ? "" : "s");

        Exception cause = (exceptions.size() == 1)
                ? exceptions.get(0)
                : new CompoundRuntimeException(msg + " - "
                + "First cause is " + exceptions.get(0) + " (listed in primary trace); "
                + "plus " + (exceptions.size() - 1) + " more (e.g. the last is " + exceptions.get(exceptions.size() - 1) + ")",
                exceptions.get(0), exceptions);

        if (Iterables.getLast(exceptions) instanceof NoMachinesAvailableException) {
            throw new NoMachinesAvailableException(msg, cause);
        } else {
            throw Exceptions.propagate(cause);
        }
    }

    protected SshMachineLocation obtainOnce(ConfigBag setup) throws NoMachinesAvailableException {
        AccessController.Response access = getManagementContext().getAccessController().canProvisionLocation(this);
        if (!access.isAllowed()) {
            throw new IllegalStateException("Access controller forbids provisioning in " + this + ": " + access.getMsg());
        }
        Stopwatch provisioningStopwatch = Stopwatch.createStarted();
        Duration provisionTimestamp;
        String globalIdentifier, fullyQualifiedDomainName, primaryIpAddress, userName, password;
        String domain = setup.get(SoftlayerBmsLocationConfig.DOMAIN);
        String location = setup.get(CLOUD_REGION_ID);
        long deadline = setup.get(SoftlayerBmsLocationConfig.WAIT_FOR_ACTIVE);
        CloudMachineNamer cloudMachineNamer = getCloudMachineNamer(setup);
        String groupId = setup.get(GROUP_ID) != null ? setup.get(GROUP_ID) : cloudMachineNamer.generateNewGroupId();
        Set<Price> prices = setup.get(SoftlayerBmsLocationConfig.PRICES);
        String hostname = cloudMachineNamer.generateNewMachineUniqueNameFromGroupId(groupId);

        try {
            Order order = new BareMetalOrderSupplier(hostname, domain, true, location, prices).get();
            Map<Object, Object> result = softLayerBmsClient.placeOrder(order);
            globalIdentifier = ((Map) Jsonya.of(result).at("orderDetails", "hardware").list().get(0)).get("globalIdentifier").toString();
            // Wait for the bare metal server to be ACTIVE
            waitForActive(globalIdentifier, deadline);
            provisionTimestamp = Duration.of(provisioningStopwatch);
            Map<Object, Object> getStatusMap = softLayerBmsClient.getObject(globalIdentifier);
            fullyQualifiedDomainName = Jsonya.of(getStatusMap).get("fullyQualifiedDomainName").toString();
            primaryIpAddress = Jsonya.of(getStatusMap).get("primaryIpAddress").toString();
            userName = ((Map) Jsonya.of(getStatusMap).at("operatingSystem", "passwords").list().get(0)).get("username").toString();
            password = ((Map) Jsonya.of(getStatusMap).at("operatingSystem", "passwords").list().get(0)).get("password").toString();
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
        LOG.info("Finished bare metal server " + setup.getDescription() + " creation:"
                + " ready after " + Duration.of(provisioningStopwatch).toStringRounded()
                + " provisioned in " + Duration.of(provisionTimestamp).toStringRounded());
        return registerSshMachineLocation(globalIdentifier, fullyQualifiedDomainName, primaryIpAddress,
                userName, password,
                location, setup);
    }

    private SshMachineLocation createSoftlayerBmsMachineLocation(String fullyQualifiedDomainName, String userName,
            String primaryIpAddress, String password, String location, ConfigBag setup) {
        return getManagementContext().getLocationManager().createLocation(LocationSpec.create(SshMachineLocation.class)
                .configure("displayName", fullyQualifiedDomainName)
                .configure("address", primaryIpAddress)
                .configure("port", 22)
                .configure("user", userName)
                .configure("password", password)
                .configureIfNotNull(CLOUD_REGION_ID, location)
                .configure(CALLER_CONTEXT, setup.get(CALLER_CONTEXT)));
    }

    protected SshMachineLocation registerSshMachineLocation(String globalIdentifier, String fullyQualifiedDomainName,
              String primaryIpAddress, String userName, String password, String location, ConfigBag setup) {
        SshMachineLocation machine = createSoftlayerBmsMachineLocation(fullyQualifiedDomainName,
                primaryIpAddress, userName, password, location, setup);
        machine.setParent(this);
        vmInstanceIds.put(machine, globalIdentifier);
        return machine;
    }

    protected CloudMachineNamer getCloudMachineNamer(ConfigBag config) {
        String namerClass = config.get(LocationConfigKeys.CLOUD_MACHINE_NAMER_CLASS);
        if (Strings.isNonBlank(namerClass)) {
            Optional<CloudMachineNamer> cloudNamer = Reflections.invokeConstructorWithArgs(getManagementContext().getCatalog().getRootClassLoader(), namerClass, config);
            if (cloudNamer.isPresent()) {
                return cloudNamer.get();
            } else {
                throw new IllegalStateException("Failed to create CloudMachineNamer " + namerClass + " for location " + this);
            }
        } else {
            return new JcloudsMachineNamer(config);
        }
    }

    protected void waitForActive(final String hardwareId, long deadline) {
        Callable<Boolean> checker = new Callable<Boolean>() {
            public Boolean call() {
                String status = softLayerBmsClient.getHardwareStatus(hardwareId);
                return status.equals("ACTIVE");
            }
        };

        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean reachable = new Repeater()
                .every(1, SECONDS)
                .until(checker)
                .limitTimeTo(deadline, HOURS)
                .run();
        if (!reachable) {
            throw new IllegalStateException("Bare metal server " + hardwareId + " failed to become ACTIVE in " + Time.makeTimeStringRounded(stopwatch));
        }

        LOG.debug("Bare metal server with hardwareId {}: is ACTIVE after {}", new Object[]{ hardwareId,
                Time.makeTimeStringRounded(stopwatch)});
        stopwatch.stop();
    }

    @Override
    public void release(SshMachineLocation machine) {
        String globalIdentifier = vmInstanceIds.remove(machine);
        if (Strings.isBlank(globalIdentifier)) {
            throw new IllegalArgumentException("Unknown machine " + machine);
        }
        LOG.info("Releasing machine {} in {}, instance id {}", new Object[]{machine, this, globalIdentifier});

        removeChild(machine);
        try {
            releaseNode(globalIdentifier);
        } catch (Exception e) {
            LOG.error("Problem releasing machine " + machine + " in " + this + ", globalIdentifier " + globalIdentifier +
                    "; discarding instance and continuing...", e);
            Exceptions.propagate(e);
        }
    }

    protected void releaseNode(String globalIdentifier) {
        if (softLayerBmsClient.getHardwareStatus(globalIdentifier).equals(ACTIVE)) {
            softLayerBmsClient.deleteServer(globalIdentifier);
        } else {
            LOG.warn("Can't delete bare metal server " + globalIdentifier);
        }
    }

    public String getRegion() {
        return getConfig(CLOUD_REGION_ID);
    }

}
