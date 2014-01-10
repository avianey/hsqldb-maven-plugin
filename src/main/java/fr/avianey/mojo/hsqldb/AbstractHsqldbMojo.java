package fr.avianey.mojo.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.hsqldb.Server;

public abstract class AbstractHsqldbMojo extends AbstractMojo {

    /**
     * The address to bind HSQLDB on.
     */
    @Parameter(property = "hsqldb.address", defaultValue = "localhost")
    public String address;

    /**
     * The port to start HSQLDB on.
     */
    @Parameter(property = "hsqldb.port")
    public int port;

    /**
     * The name to use for the database.
     */
    @Parameter(property = "hsqldb.name", defaultValue = "xdb")
    public String name;

    /**
     * The path to use for the database.
     */
    @Parameter(property = "hsqldb.path", defaultValue = "mem:test")
    public String path;

    /**
     * The username to use when authenticating.
     */
    @Parameter(property = "hsqldb.username", defaultValue = "sa")
    public String username;

    /**
     * The password to use when authenticating.
     */
    @Parameter(property = "hsqldb.password", defaultValue = "")
    public String password;

    /**
     * The absolute class name of the driver.
     */
    @Parameter(property = "hsqldb.driver", defaultValue = "org.hsqldb.jdbcDriver")
    public String driver;

    /**
     * The URL to use when connecting.
     */
    @Parameter(property = "hsqldb.connectionURL")
    public String connectionURL;
    
    /**
     * The HSQLDB validation query.
     */
    @Parameter(property = "hsqldb.validationQuery", defaultValue = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    public String validationQuery;

    /**
     * Whether to bypass running HSQLDB.
     */
    @Parameter(property = "hsqldb.skip")
    public boolean skip;

    /**
     * Shared {@link Server} instance for all mojos.
     */
    protected Server server;

    /**
     * Delegates the mojo execution to {@link #doExecute()} after initializing the {@link Server} for localhost
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping HSQLDB execution.");
            return;
        }
        setup();
        doExecute();
    }

    protected void setup() throws MojoExecutionException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Setting up HSQLDB server with :");
            getLog().debug("\taddress : " + address);
            getLog().debug("\tport : " + (port == 0 ? "default" : port));
            getLog().debug("\tname : " + name);
            getLog().debug("\tpath : " + path);
            getLog().debug("\tconnection URI : " + getConnectionURI());
            getLog().debug("\tdriver : " + driver);
            getLog().debug("\tvalidation query : " + validationQuery);
            getLog().debug("\tskip : " + skip);
        }
        try {
            server = new Server();

            // TODO : user Maven logger
            server.setLogWriter(null);
            server.setSilent(true);
            
            server.setAddress(address);
            if (port > 0) {
                server.setPort(port);
            }
            server.setDatabaseName(0, name);
            server.setDatabasePath(0, path);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Implement mojo logic here.
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected String getConnectionURI() {
        if (connectionURL != null) {
            return connectionURL;
        }
        StringBuilder sb = new StringBuilder("jdbc:hsqldb:hsql://").append(address);
        if (port > 0) {
            sb.append(":").append(port);
        }
        sb.append("/").append(name);
        return sb.toString();
    }

    protected boolean isRunning(){
        try {
            getConnection().prepareStatement(validationQuery).execute();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected boolean isClosed() throws ClassNotFoundException {
        try {
            getConnection();
        } catch (SQLException e) {
            return true;
        }
        return false;
    }
    
    protected Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
//      Class.forName(driver).newInstance();
        return DriverManager.getConnection(getConnectionURI(), username, password);
    }
    
}
