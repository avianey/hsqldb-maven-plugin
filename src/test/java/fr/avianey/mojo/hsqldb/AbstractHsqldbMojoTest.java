package fr.avianey.mojo.hsqldb;

import java.sql.DriverManager;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractHsqldbMojoTest extends AbstractMojoTestCase {

    protected static final String TARGET_TEST_CLASSES = "target/test-classes";
    protected static final String POM_PLUGIN = TARGET_TEST_CLASSES + "/poms/pom-start.xml";

    protected void configureMojo(AbstractHsqldbMojo mojo) {
        // set mojo properties
        mojo.driver = "org.hsqldb.jdbcDriver";
        mojo.address = "localhost";
        mojo.name = "xdb";
        mojo.path = "mem:test";
        mojo.username = "sa";
        mojo.password = "";
    }

    protected boolean isHsqldbUp(AbstractHsqldbMojo mojo) throws Exception {
        try {
            Class.forName(mojo.driver);
            // Class.forName(driver).newInstance();
        } catch (Exception e) {}
        return DriverManager.getConnection(mojo.getConnectionURI()).isReadOnly();
    }

}
