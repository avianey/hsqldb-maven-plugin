package fr.avianey.mojo.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public abstract class AbstractHsqldbMojoTest extends AbstractMojoTestCase {
    
    protected static final String TARGET_TEST_CLASSES = "target/test-classes";
    protected static final String POM_PLUGIN = TARGET_TEST_CLASSES + "/poms/pom-start.xml";
    private static final String VALIDATION_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

    protected void configureMojo(AbstractHsqldbMojo mojo) {
        // set mojo properties
        mojo.driver = "org.hsqldb.jdbcDriver";
        mojo.address = "localhost";
        mojo.name = "xdb";
        mojo.path = "mem:test";
        mojo.username = "sa";
        mojo.password = "";
    }

    protected boolean isRunning(AbstractHsqldbMojo mojo) throws Exception {
        try {
            Class.forName(mojo.driver);
            getConnection(mojo).prepareStatement(VALIDATION_QUERY).execute();
            // Class.forName(driver).newInstance();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected boolean isClosed(AbstractHsqldbMojo mojo) throws Exception {
        try {
            Class.forName(mojo.driver);
            getConnection(mojo);
            // Class.forName(driver).newInstance();
        } catch (SQLException e) {
            return true;
        }
        return false;
    }
    
    protected Connection getConnection(AbstractHsqldbMojo mojo) throws SQLException {
        return DriverManager.getConnection(mojo.getConnectionURI(), mojo.username, mojo.password);
    }

}
