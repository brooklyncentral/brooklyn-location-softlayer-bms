package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import brooklyn.config.BrooklynProperties;
import brooklyn.location.softlayer.bms.SoftlayerBmsLocation;
import brooklyn.location.softlayer.bms.SoftlayerBmsLocationConfig;
import brooklyn.management.internal.LocalManagementContext;

public class SoftlayerBmsLocationResolverTest {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(SoftlayerBmsLocationResolverTest.class);

    private LocalManagementContext managementContext;
    private BrooklynProperties brooklynProperties;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        managementContext = new LocalManagementContext(BrooklynProperties.Factory.newEmpty());
        brooklynProperties = managementContext.getBrooklynProperties();

        brooklynProperties.put("brooklyn.location.softlayer-bms.identity", "softlayer-bms-id");
        brooklynProperties.put("brooklyn.location.softlayer-bms.credential", "softlayer-bms-cred");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        if (managementContext != null)
            managementContext.terminate();
    }

    @Test
    public void testSoftlayerBmsDatacenterLoads() {
        SoftlayerBmsLocation loc = resolve("softlayer-bms:265592");
        assertEquals(loc.getRegion(), "265592");
    }

    @Test
    public void testSoftlayerBmsTakesProviderScopedProperties() {
        brooklynProperties.put("brooklyn.location.softlayer-bms.identity", "user");
        brooklynProperties.put("brooklyn.location.softlayer-bms.credential", "password");
        Map<String, Object> conf = resolve("softlayer-bms:region").getAllConfig(true);

        assertEquals(conf.get("identity"), "user");
        assertEquals(conf.get("credential"), "password");
    }

    @Test
    public void testSoftlayerBmsDomainLoadsAsProperty() {
        brooklynProperties.put("brooklyn.location.softlayer-bms.domain", "mytest.org");
        SoftlayerBmsLocation loc = resolve("softlayer-bms:265592");
        // just checking
        assertEquals(loc.getLocalConfigBag().getStringKey("domain"), "mytest.org");
        assertEquals(loc.getConfig(SoftlayerBmsLocationConfig.DOMAIN), "mytest.org");
    }

    private SoftlayerBmsLocation resolve(String spec) {
        return (SoftlayerBmsLocation) managementContext.getLocationRegistry().resolve(spec);
    }
}
