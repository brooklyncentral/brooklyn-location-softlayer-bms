package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import brooklyn.entity.rebind.RebindTestUtils;
import brooklyn.location.Location;
import brooklyn.location.softlayer.bms.SoftlayerBmsLocation;
import brooklyn.management.internal.LocalManagementContext;

@Test(groups = "Integration")
public class SoftLayerBmsLocationRebindTest extends AbstractSoftlayerBmsLocationLiveTest {

    private static final Logger LOG = LoggerFactory.getLogger(SoftLayerBmsLocationRebindTest.class);

    private ClassLoader classLoader = getClass().getClassLoader();
    private File mementoDir;

    @AfterMethod
    @Override
    public void tearUp() throws Exception {
        super.tearUp();
        if (mementoDir != null) RebindTestUtils.deleteMementoDir(mementoDir);
    }

    @Override
    protected LocalManagementContext newLocalManagementContext() {
        mementoDir = Files.createTempDir();
        return RebindTestUtils.newPersistingManagementContext(mementoDir, classLoader, 1);
    }

    @Test
    public void testLocationRebind() throws Exception {
        SoftlayerBmsLocation originalSoftlayerBmsLocation = softlayerBmsLocation;
        RebindTestUtils.waitForPersisted(managementContext);

        LocalManagementContext newManagementContext = RebindTestUtils.newPersistingManagementContextUnstarted(mementoDir, classLoader);
        newManagementContext.getRebindManager().rebind(classLoader);
        newManagementContext.getRebindManager().start();

        Optional<Location> optionalSoftlayerBmsLocation = Iterables.tryFind(managementContext.getLocationManager().getLocations(),
                Predicates.instanceOf(SoftlayerBmsLocation.class));
        assertTrue(optionalSoftlayerBmsLocation.isPresent());
        SoftlayerBmsLocation softlayerBmsLocation = (SoftlayerBmsLocation) optionalSoftlayerBmsLocation.get();
        assertSoftlayerBmsLocationEquals(originalSoftlayerBmsLocation, softlayerBmsLocation);
    }
    private void assertSoftlayerBmsLocationEquals(SoftlayerBmsLocation actual, SoftlayerBmsLocation expected) {
        String errmsg = "actual="+actual.toVerboseString()+"; expected="+expected.toVerboseString();
        assertEquals(actual.getId(), expected.getId(), errmsg);
        assertEquals(actual.getDisplayName(), expected.getDisplayName());
        assertEquals(actual.getRegion(), expected.getRegion());
        assertEquals(actual.getRegion(), expected.getRegion());
        assertEquals(actual.getVmInstanceIds(), expected.getVmInstanceIds());
    }

}
