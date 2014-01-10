hsqldb-maven-plugin
===================

Start and Stop HSQLDB in server Mode directly from Maven.

```xml
<plugin>

    <!-- current version -->
    <groupId>fr.avianey.mojo</groupId>
    <artifactId>hsqldb-maven-plugin</artifactId>
    <version>1.0.0</version>
    
    <!-- 
        default value for in memory jdbc:hsqldb:hsql://localhost/xdb
        override only values you want to change
    -->
    <configuration>
        <driver>org.hsqldb.jdbcDriver</driver>
        <path>mem:test</path>
        <address>localhost</address>
        <name>xdb</name>
        <username>sa</username>
        <password></password>
        <validationQuery>SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS</validationQuery>
    </configuration>

    <!-- call start and stop -->
    <executions>
        <execution>
            <id>start-hsqldb</id>
            <phase>pre-integration-test</phase>
            <goals>
                <goal>start</goal>
            </goals>
        </execution>
        <execution>
            <id>stop-hsqldb</id>
            <phase>post-integration-test</phase>
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

### Parent

The parent pom defines two modules :
- **web** : a war to be deployed in a container
- **tests** : integration tests to be run against the deploy war

```
+ parent
|-- pom.xml
|
|-- + web
|   |-- pom.xml
|   |-- src/main/resources/META-INF/persistence.xml
|   |-- src/main/resources/META-INF/resources.xml
|
|-- + tests
|   |-- pom.xml
|   |-- src/test/java/SimpleTest.java
```

The **tests** module is activated with the *tests* profile with its own database configuration.

pom.xml :
```xml
<project>

    ...

    <modules>
        <module>web</module>
    </modules>
    
	<profiles>
		<profile>
			<id>dev</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<bd.driver>com.mysql.jdbc.Driver</bd.driver>
				<bd.dialect>org.eclipse.persistence.platform.database.MySQLPlatform</bd.dialect>
				<bd.url>jdbc:mysql://127.0.0.1:3306/app</bd.url>
				<bd.username>root</bd.username>
				<bd.password></bd.password>
				<bd.validationQuery>SELECT 1</bd.validationQuery>
				<bd.generation>create-or-extend-tables</bd.generation>
                <bd.datasourceCreator>tomcat</bd.datasourceCreator>
			</properties>
		</profile>
		<!-- lancement des tests unitaires -->
		<profile>
			<id>tests</id>
			<modules>
				<module>tests</module>
			</modules>
			<properties>
				<!-- hsqldb -->
				<hsqldb.address>localhost</hsqldb.address>
				<hsqldb.name>xdb</hsqldb.name>
				<hsqldb.path>mem:test</hsqldb.path>
				<hsqldb.username>sa</hsqldb.username>
				<hsqldb.password></hsqldb.password>
				<hsqldb.driver>org.hsqldb.jdbcDriver</hsqldb.driver>
				<hsqldb.connectionURL>jdbc:hsqldb:hsql://${hsqldb.address}/${hsqldb.name}</hsqldb.connectionURL>
				<hsqldb.validationQuery>SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS</hsqldb.validationQuery>
				<!-- override defaults -->
				<bd.driver>${hsqldb.driver}</bd.driver>
				<bd.dialect>org.eclipse.persistence.platform.database.HSQLPlatform</bd.dialect>
				<bd.url>${hsqldb.connectionURL}</bd.url>
				<bd.username>${hsqldb.username}</bd.username>
				<bd.password>${hsqldb.password}</bd.password>
				<bd.validationQuery>${hsqldb.validationQuery}</bd.validationQuery>
				<bd.generation>create-or-extend-tables</bd.generation>
                <bd.datasourceCreator>tomcat</bd.datasourceCreator>
			</properties>
		</profile>
	</profiles>

    ...

</project>	
```

### Web project to test

This is a simple **jpa** war with the following configuration :

persistence.xml :
```xml
<persistence version="2.0"
             xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
                       http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">

    <persistence-unit name="myapp">
        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
        <jta-data-source>openejb:Resource/JTA_Datasource</jta-data-source>
        <exclude-unlisted-classes>false</exclude-unlisted-classes>
        <properties>
            <property name="eclipselink.target-database" value="${bd.dialect}"/>
            <property name="eclipselink.ddl-generation" value="${bd.generation}" />
            <property name="eclipselink.ddl-generation.output-mode" value="database" />
            <property name="eclipselink.logging.level" value="FINE"/>
            <property name="eclipselink.logging.level.sql" value="FINE"/>
            <property name="eclipselink.logging.parameters" value="true"/>
        </properties>
    </persistence-unit>

</persistence>
```

resources.xml :
```xml
<resources>
    <Resource id="JTA_Datasource" type="javax.sql.DataSource">
        # http://tomee.apache.org/containers-and-resources.html
        # configure the pool
        DataSourceCreator = ${bd.datasourceCreator}

        # it is a jta datasource
        JtaManaged = true

        # tomcat pool configuration
        driverClassName = ${bd.driver}
        url = ${bd.url}
        username = ${bd.username}
        password = ${bd.password}
        validationQuery = ${bd.validationQuery}

        # specific to tomcat pooling
        jmxEnabled = true
    </Resource>
</resources>
```


### Test project

The test project will :
1- start the HSQLDB server
2- start the container (tomee) with which is responsible of deploying the **war**
3- test the deployed **war** application
   1- connect to the HSQLDB server
   2- setup the required data in the database
   3- run remote tests against the deployed application
4- stop the container (tomee)
5- stop the HSQLDB server

pom.xml :
```xml
<project>
    
    ...

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>fr.avianey.mojo</groupId>
                    <artifactId>hsqldb-maven-plugin</artifactId>
                    <!-- shared config for start and stop -->
                    <configuration>
                        <driver>${hsqldb.driver}</driver>
                        <path>${hsqldb.path}</path>
                        <address>${hsqldb.address}</address>
                        <name>${hsqldb.name}</name>
                        <username>${hsqldb.username}</username>
                        <password>${hsqldb.password}</password>
                        <validationQuery>${hsqldb.validationQuery}</validationQuery>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <configuration>
                    <skipTests>false</skipTests>
                    <includes>
                        <include>**/SimpleTest.java</include>
                    </includes>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!-- start HSQLDB before tomee -->
            <plugin>
                <groupId>fr.avianey.mojo</groupId>
                <artifactId>hsqldb-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>start-hsqldb</id>
                        <phase>pre-integration-test</phase>
                        <goals>
                            <goal>start</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.openejb.maven</groupId>
                <artifactId>tomee-maven-plugin</artifactId>
                <configuration>
                    
                    ...
                    
                </configuration>
                <executions>
                    <execution>
                        <id>start-tomee</id>
                        <phase>pre-integration-test</phase>
                        <goals>
                            <goal>start</goal>
                        </goals>
                        <configuration>
                            <checkStarted>true</checkStarted>
                        </configuration>
                    </execution>
                    <execution>
                        <id>stop-tomee</id>
                        <phase>post-integration-test</phase>
                        <goals>
                            <goal>stop</goal>
                        </goals>
                        <configuration>
                            <simpleLog>true</simpleLog>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- stop HSQLDB after tomee -->
            <plugin>
                <groupId>fr.avianey.mojo</groupId>
                <artifactId>hsqldb-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>stop-hsqldb</id>
                        <phase>post-integration-test</phase>
                        <goals>
                            <goal>stop</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

SimpleTest.java :
```java
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.TargetServer;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTest {
    
    static EntityManagerFactory emf = null;

    @BeforeClass
    public static void init() {
        Map<String, String> properties = new HashMap<String, String>();

        // Ensure RESOURCE_LOCAL transactions is used.
        properties.put(PersistenceUnitProperties.TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());

        // Configure the internal EclipseLink connection pool
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.hsqldb.jdbcDriver");
        properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:hsqldb:hsql://localhost/xdb");
        properties.put(PersistenceUnitProperties.JDBC_USER, "sa");
        properties.put(PersistenceUnitProperties.JDBC_PASSWORD, "");

        // Configure logging. FINE ensures all SQL is shown
        properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");

        // Ensure that no server-platform is configured
        properties.put(PersistenceUnitProperties.TARGET_SERVER, TargetServer.None);
        
        emf = (new PersistenceProvider()).createEntityManagerFactory("myapp", properties);
        
        // do whatever you want with your EntityManagerFactory
    }
    
    @Test
    public void test() {
        // test remote services here
        // jax-rs, jax-ws, URLConnection, ...
    }

}
```

### Results

Run :

    mvn clean install -Ptests

And see :

```
[INFO] --- hsqldb-maven-plugin:1.0.0:start (start-hsqldb) @ tests ---
[INFO] HSQLDB server started on jdbc:hsqldb:hsql://localhost/xdb
[INFO] 
[INFO] --- tomee-maven-plugin:1.6.0:start (start-tomee) @ tests ---
...
[INFO] --- maven-failsafe-plugin:2.16:integration-test (default) @ tests ---
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
...
[INFO] --- hsqldb-maven-plugin:1.0.0:stop (stop-hsqldb) @ tests ---
[INFO] 
[INFO] --- tomee-maven-plugin:1.6.0:stop (stop-tomee) @ tests ---
```
