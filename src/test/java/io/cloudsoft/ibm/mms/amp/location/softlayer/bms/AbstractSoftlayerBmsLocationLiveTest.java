package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.ImmutableMap;

import brooklyn.config.BrooklynProperties;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.Entities;
import brooklyn.location.LocationSpec;
import brooklyn.location.jclouds.JcloudsLocationConfig;
import brooklyn.location.softlayer.bms.SoftlayerBmsLocation;
import brooklyn.management.internal.LocalManagementContext;
import brooklyn.test.entity.LocalManagementContextForTests;
import brooklyn.util.collections.MutableMap;

public abstract class AbstractSoftlayerBmsLocationLiveTest {
    protected BrooklynProperties brooklynProperties;
    protected LocalManagementContext managementContext;
    protected SoftlayerBmsLocation softlayerBmsLocation;

    @BeforeMethod(alwaysRun=true)
    public void setUp() throws Exception {
        managementContext = newLocalManagementContext();
        brooklynProperties = new LocalManagementContext().getBrooklynProperties();
        softlayerBmsLocation = newSampleSoftlayerBmsLocationForTesting(ImmutableMap.<ConfigKey<?>,Object>of());
    }

    protected LocalManagementContext newLocalManagementContext() {
        return new LocalManagementContextForTests();
    }

    @AfterMethod(alwaysRun=true)
    public void tearUp() throws Exception {
        if (managementContext != null) Entities.destroyAll(managementContext);
    }

    protected SoftlayerBmsLocation newSampleSoftlayerBmsLocationForTesting(Map<? extends ConfigKey<?>,?> config) {
        String identity = (String) brooklynProperties.get("brooklyn.location.jclouds.softlayer.identity");
        if (identity == null) identity = (String) brooklynProperties.get("brooklyn.softlayer.identity");
        String credential = (String) brooklynProperties.get("brooklyn.location.jclouds.softlayer.credential");
        if (credential == null) credential = (String) brooklynProperties.get("brooklyn.softlayer.credential");

        Map<ConfigKey<?>,?> allConfig = MutableMap.<ConfigKey<?>,Object>builder()
                .put(JcloudsLocationConfig.CLOUD_PROVIDER, "softlayer-bms")
                .put(JcloudsLocationConfig.CLOUD_REGION_ID, "265592") // ams01
                .put(JcloudsLocationConfig.ACCESS_IDENTITY, identity)
                .put(JcloudsLocationConfig.ACCESS_CREDENTIAL, credential)
                .putAll(config)
                .build();

        LocationSpec<SoftlayerBmsLocation> spec = LocationSpec.create(SoftlayerBmsLocation.class).configure(allConfig);
        try {
            return managementContext.getLocationManager().createLocation(spec);
        } catch (NullPointerException e) {
            throw new AssertionError("Failed to create " + SoftlayerBmsLocation.class.getName() +
                    ". Have you configured brooklyn.location.jclouds.softlayer.{identity,credential} in your " +
                    "brooklyn.properties file?");
        }
    }
}
