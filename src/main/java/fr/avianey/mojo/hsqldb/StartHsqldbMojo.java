package fr.avianey.mojo.hsqldb;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "start", requiresProject = false)
public class StartHsqldbMojo extends AbstractHsqldbMojo {

    /**
     * Whether to fail, if there's already something running on the port.
     */
    @Parameter(property = "derby.fail.if.already.running", defaultValue = "true")
    public boolean failIfAlreadyRunning;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
//            try {
//                getLog().info("Starting the Derby server ...");
//                server.start(new PrintWriter(System.out));
//            } catch (Exception e) {
//                if (e instanceof BindException) {
//                    if (failIfAlreadyRunning) {
//                        throw new MojoExecutionException("Failed to start the Derby server, port already open!", e);
//                    } else {
//                        getLog().info("Derby is already running.");
//                    }
//                } else {
//                    throw new MojoExecutionException(e.getMessage(), e);
//                }
//            }

            if (server != null) {
                server.setDaemon(true);
                server.start();
            } else {
                throw new MojoExecutionException("Failed to start the Derby server!");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
