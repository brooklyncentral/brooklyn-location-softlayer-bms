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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import brooklyn.location.Location;
import brooklyn.location.LocationRegistry;
import brooklyn.location.LocationResolver;
import brooklyn.location.LocationSpec;
import brooklyn.location.basic.BasicLocationRegistry;
import brooklyn.location.basic.LocationInternal;
import brooklyn.location.basic.LocationPropertiesFromBrooklynProperties;
import brooklyn.location.jclouds.JcloudsLocationConfig;
import brooklyn.management.ManagementContext;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.text.Strings;

public class SoftlayerBmsResolver implements LocationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SoftlayerBmsResolver.class);
    public static final String SOFTLAYER_BMS = "softlayer-bms";

    public static final Pattern PATTERN = Pattern.compile("("+SOFTLAYER_BMS+"|"+SOFTLAYER_BMS.toUpperCase()+")" + ":([a-zA-Z0-9]+)" +
            "(:([a-zA-Z0-9]+))?" + "(:\\((.*)\\))?$");

    private ManagementContext managementContext;

    @Override
    public void init(ManagementContext managementContext) {
        this.managementContext = checkNotNull(managementContext, "managementContext");
    }

    @Override
    public String getPrefix() {
        return SOFTLAYER_BMS;
    }

    @Override
    public boolean accepts(String spec, LocationRegistry registry) {
        return BasicLocationRegistry.isResolverPrefixForSpec(this, spec, true);
    }

    @Override
    public Location newLocationFromString(Map locationFlags, String spec, brooklyn.location.LocationRegistry registry) {
        return newLocationFromString(spec, registry, registry.getProperties(), locationFlags);
    }

    protected SoftlayerBmsLocation newLocationFromString(String spec, brooklyn.location.LocationRegistry registry, Map properties, Map locationFlags) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving location '" + spec + "' with flags " + Joiner.on(",").withKeyValueSeparator("=").join(locationFlags));
        }
        String namedLocation = (String) locationFlags.get(LocationInternal.NAMED_SPEC_NAME.getName());

        Matcher matcher = PATTERN.matcher(spec);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid location '"+spec+"';");
        }
        String datacenter = matcher.group(2);
        if (Strings.isBlank(datacenter)) {
            throw new IllegalArgumentException("Invalid location '"+spec+"'; datacenter id must be non-empty");
        }
        Map<String, Object> filteredProperties = new LocationPropertiesFromBrooklynProperties().getLocationProperties(SOFTLAYER_BMS, namedLocation, properties);
        MutableMap<String, Object> flags = MutableMap.<String, Object>builder().putAll(filteredProperties).putAll(locationFlags).build();

        LocationSpec<SoftlayerBmsLocation> locationSpec = LocationSpec.create(SoftlayerBmsLocation.class)
                .configure(flags)
                .configure(JcloudsLocationConfig.CLOUD_REGION_ID.getName(), datacenter)
                .displayName(spec);
        return managementContext.getLocationManager().createLocation(locationSpec);
    }
}
