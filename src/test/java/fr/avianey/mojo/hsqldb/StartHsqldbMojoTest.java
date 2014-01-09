package fr.avianey.mojo.hsqldb;

import org.junit.Assert;
import org.junit.Test;

public class StartHsqldbMojoTest extends AbstractHsqldbMojoTest {

    StartHsqldbMojo startMojo;
    StopHsqldbMojo stopMojo;

    protected void setUp() throws Exception {
        super.setUp();

        startMojo = (StartHsqldbMojo) lookupMojo("start", POM_PLUGIN);
        configureMojo(startMojo);

        stopMojo = (StopHsqldbMojo) lookupMojo("stop", POM_PLUGIN);
        configureMojo(stopMojo);
    }

    @Test
    public void test() throws Exception {
        startMojo.execute();
        Assert.assertFalse(startMojo.skip);
        Assert.assertFalse(isHsqldbUp(startMojo));
        stopMojo.execute();
        Assert.assertTrue(isHsqldbUp(startMojo));
    }

}
