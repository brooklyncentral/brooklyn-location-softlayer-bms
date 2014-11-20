package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableMap;

@Test(groups = "Live")
public class SoftlayerBmsLocationLiveTest extends AbstractSoftlayerBmsLocationLiveTest {

    @Test
    public void testCreateAndReleaseBms() throws Exception {
        SshMachineLocation machine = softlayerBmsLocation.obtain(MutableMap.of());
        assertNotNull(machine);
        softlayerBmsLocation.release(machine);
    }

}
