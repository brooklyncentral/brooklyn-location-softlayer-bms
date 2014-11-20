package io.cloudsoft.ibm.mms.amp.location.softlayer.bms;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import brooklyn.entity.basic.ApplicationBuilder;
import brooklyn.entity.basic.EmptySoftwareProcess;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.rebind.RebindTestUtils;
import brooklyn.location.OsDetails;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.location.softlayer.bms.SoftlayerBmsLocation;
import brooklyn.management.internal.LocalManagementContext;
import brooklyn.test.entity.TestApplication;
import brooklyn.test.entity.TestEntity;

@Test(groups = "Live")
public class SoftLayerBmsLocationRebindLiveTest extends AbstractSoftlayerBmsLocationLiveTest {

    private static final Logger LOG = LoggerFactory.getLogger(SoftLayerBmsLocationRebindLiveTest.class);

    private ClassLoader classLoader = getClass().getClassLoader();
    private TestApplication origApp;
    private EmptySoftwareProcess origEntity;
    private File mementoDir;

    @BeforeMethod
    @Override
    public void setUp() throws Exception {
        super.setUp();
        origApp = ApplicationBuilder.newManagedApp(EntitySpec.create(TestApplication.class), managementContext);
        origEntity = origApp.createAndManageChild(EntitySpec.create(EmptySoftwareProcess.class));
    }

    @AfterMethod
    @Override
    public void tearUp() throws Exception {
        super.tearUp();
        if (origApp != null) Entities.destroyAll(origApp.getManagementContext());
        if (mementoDir != null) RebindTestUtils.deleteMementoDir(mementoDir);
    }

    @Override
    protected LocalManagementContext newLocalManagementContext() {
        mementoDir = Files.createTempDir();
        return RebindTestUtils.newPersistingManagementContext(mementoDir, classLoader, 1);
    }

    @Test(groups="Live")
    public void testRebindsToSshMachine() throws Exception {
        origApp.start(ImmutableList.of(softlayerBmsLocation));
        SoftlayerBmsLocation originalSoftlayerBmsLocation = softlayerBmsLocation;
        LOG.debug("orig locations: " + origEntity.getLocations());
        SshMachineLocation origMachine = (SshMachineLocation) Iterables.find(origEntity.getLocations(), Predicates.instanceOf(SshMachineLocation.class));

        TestApplication newApp = rebind();
        TestEntity newEntity = (TestEntity) Iterables.find(newApp.getChildren(), Predicates.instanceOf(TestEntity.class));
        SshMachineLocation newMachine = (SshMachineLocation) Iterables.find(newEntity.getLocations(), Predicates.instanceOf(SshMachineLocation.class));

        assertMachineEquals(newMachine, origMachine);
        assertTrue(newMachine.isSshable());

        SoftlayerBmsLocation newSoftlayerBmsLocation = (SoftlayerBmsLocation) newMachine.getParent();
        assertSoftlayerBmsLocationEquals(newSoftlayerBmsLocation, originalSoftlayerBmsLocation);
    }

    private void assertMachineEquals(SshMachineLocation actual, SshMachineLocation expected) {
        String errmsg = "actual="+actual.toVerboseString()+"; expected="+expected.toVerboseString();
        assertEquals(actual.getId(), expected.getId(), errmsg);
        assertOsDetailEquals(actual.getOsDetails(), expected.getOsDetails());
        assertEquals(actual.getSshHostAndPort(), expected.getSshHostAndPort());
    }

    private void assertOsDetailEquals(OsDetails actual, OsDetails expected) {
        String errmsg = "actual="+actual+"; expected="+expected;
        if (actual == null) assertNull(expected, errmsg);
        assertEquals(actual.isWindows(), expected.isWindows());
        assertEquals(actual.isLinux(), expected.isLinux());
        assertEquals(actual.isMac(), expected.isMac());
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getArch(), expected.getArch());
        assertEquals(actual.getVersion(), expected.getVersion());
        assertEquals(actual.is64bit(), expected.is64bit());
    }

    private void assertSoftlayerBmsLocationEquals(SoftlayerBmsLocation actual, SoftlayerBmsLocation expected) {
        String errmsg = "actual="+actual.toVerboseString()+"; expected="+expected.toVerboseString();
        assertEquals(actual.getId(), expected.getId(), errmsg);
        assertEquals(actual.getRegion(), expected.getRegion(), errmsg);
        assertEquals(actual.getHostGeoInfo(), expected.getHostGeoInfo(), errmsg);
    }

    private TestApplication rebind() throws Exception {
        RebindTestUtils.waitForPersisted(origApp);
        return (TestApplication) RebindTestUtils.rebind(mementoDir, getClass().getClassLoader());
    }
}
