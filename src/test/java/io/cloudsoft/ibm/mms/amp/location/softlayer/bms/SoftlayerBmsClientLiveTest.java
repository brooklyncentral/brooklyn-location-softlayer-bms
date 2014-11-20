package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import brooklyn.config.BrooklynProperties;
import brooklyn.entity.basic.Entities;
import brooklyn.location.softlayer.bms.client.SoftLayerBmsClient;
import brooklyn.location.softlayer.bms.domain.BareMetalOrderSupplier;
import brooklyn.location.softlayer.bms.domain.DefaultPricesSupplier;
import brooklyn.location.softlayer.bms.domain.Order;
import brooklyn.management.internal.LocalManagementContext;
import brooklyn.util.collections.Jsonya;
import brooklyn.util.repeat.Repeater;
import brooklyn.util.text.Strings;

@Test(groups = "Live")
public class SoftlayerBmsClientLiveTest {

    private BrooklynProperties brooklynProperties;
    private LocalManagementContext managementContext;
    private SoftLayerBmsClient softLayerBmsClient;
    private String globalIdentifier;
    private Order order;

    @BeforeMethod(alwaysRun=true)
    public void setUp() throws Exception {
        managementContext = new LocalManagementContext();
        brooklynProperties = managementContext.getBrooklynProperties();
        String identity = (String) brooklynProperties.get("brooklyn.location.jclouds.softlayer.identity");
        String credential = (String) brooklynProperties.get("brooklyn.location.jclouds.softlayer.credential");
        softLayerBmsClient = new SoftLayerBmsClient(identity, credential);
    }

    @AfterMethod(alwaysRun=true)
    public void tearUp() throws Exception {
        if (managementContext != null) Entities.destroyAll(managementContext);
    }

    @Test
    public void testPlaceOrder() throws Exception {
        order = new BareMetalOrderSupplier(this.getClass().getSimpleName() + new Random().nextInt(), "test.org",
                true, "265592", new DefaultPricesSupplier().get()).get();
        Map result = softLayerBmsClient.placeOrder(order);
        globalIdentifier = ((Map) Jsonya.of(result).at("orderDetails", "hardware").list().get(0)).get("globalIdentifier").toString();
        assertNotNull(globalIdentifier);
        assertTrue(waitForActive(globalIdentifier));
    }

    @Test(dependsOnMethods = "testPlaceOrder")
    public void testPowerOffServer() throws Exception {
        if (Strings.isBlank(globalIdentifier)) {
            fail();
        }
        assertTrue(softLayerBmsClient.powerOff(globalIdentifier));
    }

    @Test(dependsOnMethods = "testPowerOffServer")
    public void testPowerOnServer() throws Exception {
        if (Strings.isBlank(globalIdentifier)) {
            fail();
        }
        assertTrue(softLayerBmsClient.powerOn(globalIdentifier));
        assertTrue(waitForActive(globalIdentifier));
    }

    @Test(dependsOnMethods = "testPowerOnServer")
    public void testRebootSoftServer() throws Exception {
        if (Strings.isBlank(globalIdentifier)) {
            fail();
        }
        assertTrue(softLayerBmsClient.rebootSoft(globalIdentifier));
        assertTrue(waitForActive(globalIdentifier));
    }

    @Test(dependsOnMethods = "testPowerOnServer")
    public void testRebootHardServer() throws Exception {
        if (Strings.isBlank(globalIdentifier)) {
            fail();
        }
        assertTrue(softLayerBmsClient.rebootHard(globalIdentifier));
        assertTrue(waitForActive(globalIdentifier));
    }

    @Test(dependsOnMethods = "testRebootHardServer")
    public void testDeleteServer() throws Exception {
        if (Strings.isBlank(globalIdentifier)) {
            fail();
        }
        softLayerBmsClient.deleteServer(globalIdentifier);
    }

    private boolean waitForActive(final String globalIdentifier) {
        Callable<Boolean> checker = new Callable<Boolean>() {
            public Boolean call() {
                String status = softLayerBmsClient.getHardwareStatus(globalIdentifier);
                return status.equals("ACTIVE");
            }
        };
        return new Repeater()
                .every(1, SECONDS)
                .until(checker)
                .limitTimeTo(12, HOURS)
                .run();
    }

}
