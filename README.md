hsqldb-maven-plugin
===================

Start and Stop HSQLDB in server Mode directly from Maven.

```xml
<plugin>
    <groupId>fr.avianey.mojo</groupId>
    <artifactId>hsqldb-maven-plugin</artifactId>

    <configuration>
        
    </configuration>

    <executions>
        <execution>
            <id>start</id>
            <goals>
                <goal>start</goal>
            </goals>
        </execution>
        <execution>
            <id>stop</id>
            <goals>
                <goal>stop</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
   
## How to use

Use this plugin when you want to run **integration-test** with [maven-failsafe-plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) to test services from a client point of view on a remote that is launched by maven during the **pre-integration-test** phase :  

1. **pre-integration-test** :
   1. start an HSQLDB Server
   2. start a container like jetty (jetty:start) or tomee (tomee:start)
   3. deploy the application to test in the container
2. **integration-test** :
   1. setup your test data in HSQLDB for use by the deployed application
   2. test the application (JAX-RS services, JAX-WS services)
3. **pre-integration-test** :
   1. stop the container
   2. stop the HSQLDB Server

## Sample

### Parent pom

two modules
two profiles
test module activated with test profile only

### Project to test

simple JAX-RS app with MySQL in PROD and HSQLDB in test

### Test project

setup HSQLDB
start tomee
setup
test
stop tomee
stop HSQLDB
## Licence
