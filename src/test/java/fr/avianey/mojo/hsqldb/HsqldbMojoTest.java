package fr.avianey.mojo.hsqldb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HsqldbMojoTest {

    private StartHsqldbMojo startMojo;
    private StopHsqldbMojo stopMojo;

    private static void configureMojo(AbstractHsqldbMojo mojo) throws MojoExecutionException {
        mojo.driver = "org.hsqldb.jdbcDriver";
        mojo.address = "localhost";
        mojo.name = "xdb";
        mojo.path = "mem:test";
        mojo.username = "sa";
        mojo.password = "";
        mojo.validationQuery = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
        mojo.skip = false;
    }

    @Before
    public void init() throws Exception {
        startMojo = new StartHsqldbMojo();
        stopMojo = new StopHsqldbMojo();
        configureMojo(startMojo);
        configureMojo(stopMojo);
        startMojo.failIfAlreadyRunning = false;
        stopMojo.failIfNotRunning = false;
        
        if (startMojo.isRunning()) {
            Assert.fail("HSQLDB should not be running before a test is run...");
        }
    }
    
    @After
    public void stop() throws MojoExecutionException, MojoFailureException {
        stopMojo.failIfNotRunning = false;
        stopMojo.execute();
    }

    @Test
    public void simple() throws Exception {
        startMojo.execute();
        Assert.assertTrue(startMojo.isRunning());
        stopMojo.execute();
        Assert.assertTrue(stopMojo.isClosed());
    }

    @Test
    public void alreadyStarted() throws Exception {
        startMojo.execute();
        Assert.assertTrue(startMojo.isRunning());
        startMojo.failIfAlreadyRunning = true;
        try {
            startMojo.execute();
            Assert.fail();
        } catch (MojoExecutionException e) {
            Assert.assertEquals("Failed to start the HSQLDB server, the server is already running on " + startMojo.getConnectionURI(), e.getMessage());
        }
        Assert.assertTrue(startMojo.isRunning());
    }

    @Test
    public void skip() throws Exception {
        startMojo.execute();
        Assert.assertTrue(startMojo.isRunning());
        startMojo.failIfAlreadyRunning = true;
        startMojo.skip = true;
        startMojo.execute();
        Assert.assertTrue(startMojo.isRunning());
    }

}
