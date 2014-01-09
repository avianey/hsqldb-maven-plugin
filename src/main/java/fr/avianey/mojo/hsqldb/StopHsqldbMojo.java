package fr.avianey.mojo.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "stop", requiresProject = false)
public class StopHsqldbMojo extends AbstractHsqldbMojo {

    /**
     * Whether to fail, if Derby is not running.
     */
    @Parameter(property = "fail.if.not.running", defaultValue = "false")
    public boolean failIfNotRunning;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
//            try {
//                server.ping();
//            } catch (Exception e) {
//                if (failIfNotRunning) {
//                    throw new MojoExecutionException("Failed to stop the Derby server, no server running!", e);
//                }
//
//                getLog().error("Derby server was already stopped.");
//                return;
//            }

            try {
                
                try {
                    Class.forName(driver);
                    // Class.forName(driver).newInstance();
                } catch (Exception e) {
                    throw new MojoExecutionException("Failed to load HSQLDB JDBC driver.", e);
                }
                Connection c = DriverManager.getConnection(getConnectionURI(), "sa", "");
                c.prepareStatement("shutdown").execute();
                
            } catch (SQLException e) {
                getLog().error(e);
            }

//          server.shutdown();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
