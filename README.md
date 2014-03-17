webjars-utils
=============

Utilities to integrate WebJars into a complex development environment

## Maven plugin
The maven plugin search for all WebJars dependencies and writes a dependency graph into a file. This file must be then provided to a Spring's component to serve static content.

Plugin configuration:

```
  <plugin>
    <groupId>com.github.seykron</groupId>
    <artifactId>webjars-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
      <outputFile>${basedir}/src/main/resources/META-INF/deps.js</outputFile>
    </configuration>
    <executions>
      <execution>
        <id>build-dependencies</id>
        <phase>validate</phase>
        <goals>
          <goal>build-dependencies</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```

Be sure that the specified directory already exist, the plugin will throw an exception if the directory does not exist.

## Spring integration
In the previous a dependency graph has been written to a file. Now, a new Spring's HttpRequestHandler must be mapped into the spring context configuration (usually spring-servlet.xml) in order to map WebJars resources.

Add the maven dependency to your POM:

```
  <!-- Webjars -->
  <dependency>
    <groupId>org.webjars</groupId>
    <artifactId>jquery</artifactId>
    <version>2.1.0-2</version>
  </dependency>

  <!-- Spring MVC integration -->
  <dependency>
    <groupId>com.github.seykron</groupId>
    <artifactId>spring-webjars</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
```

And then map the spring configuration in your spring-servlet:

```
  <bean name="resourceHandler"
    class="com.github.seykron.webjars.WebJarsRequestHandler">
    <constructor-arg index="0" value="/META-INF/deps.js" />
    <property name="locations">
      <list>
        <value>/lib/</value>
      </list>
    </property>
  </bean>

  <bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    <property name="urlMap">
      <map>
        <entry key="/lib/**" value-ref="resourceHandler"/>
      </map>
    </property>
  </bean>
```

It should be working at /yourApp/lib/webjars/jquery/2.1.0-2/jquery.min.js

For further information visit WebJars documentation, Spring MVC section:
http://www.webjars.org/documentation

