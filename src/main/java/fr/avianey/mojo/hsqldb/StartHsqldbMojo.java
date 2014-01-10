package fr.avianey.mojo.hsqldb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.hsqldb.server.ServerConstants;

@Mojo(name = "start", requiresProject = false)
public class StartHsqldbMojo extends AbstractHsqldbMojo {

    /**
     * Whether to fail, if there's already something running on the port.
     */
    @Parameter(property = "hsqldb.failIfAlreadyRunning", defaultValue = "true")
    public boolean failIfAlreadyRunning;

    @Override
    public void doExecute() throws MojoExecutionException {
        try {
            if (isRunning()) {
                if (failIfAlreadyRunning) {
                    throw new MojoExecutionException("Failed to start the HSQLDB server, the server is already running on " + getConnectionURI());
                }
                getLog().warn("HSQLDB server is already running on " + getConnectionURI());
                return;
            } else {
                // try to start it
                if (server != null) {
                    server.setDaemon(true);
                    server.start();
                    switch (server.getState()) {
                    case ServerConstants.SERVER_STATE_CLOSING:
                    case ServerConstants.SERVER_STATE_SHUTDOWN:
                        if (failIfAlreadyRunning) {
                            throw new MojoExecutionException("Failed to start the HSQLDB server");
                        }
                        break;
                    }
                    getLog().info("HSQLDB server started on " + getConnectionURI());
                } else {
                    throw new MojoExecutionException("Failed to start the HSQLDB server");
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
