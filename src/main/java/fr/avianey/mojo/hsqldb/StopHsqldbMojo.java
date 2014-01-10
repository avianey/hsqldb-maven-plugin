package fr.avianey.mojo.hsqldb;

import java.sql.SQLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "stop", requiresProject = false)
public class StopHsqldbMojo extends AbstractHsqldbMojo {

    /**
     * Whether to fail, if HSQLDB is not running.
     */
    @Parameter(property = "hsqldb.failIfNotRunning", defaultValue = "false")
    public boolean failIfNotRunning;

    @Override
    public void doExecute() throws MojoExecutionException {
        try {
            if (isClosed()) {
                if (failIfNotRunning) {
                    throw new MojoExecutionException("Failed to stop the HSQLDB server, no server running");
                }
                getLog().warn("HSQLDB server is already stopped");
                return;
            }
            try {
                getConnection().prepareStatement("shutdown").execute();
            } catch (SQLException e) {
                if (isClosed()) {
                    getLog().warn("A problem occured while sending the shutdown command to the HSQLDB server", e);
                } else {
                    throw new MojoExecutionException("Failed to stop the HSQLDB server", e);
                }
            }

//          server.shutdown();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
