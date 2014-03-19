webjars-utils
=============

Utilities to integrate WebJars into a complex development environment.

## Table of Contents
* [Background](#background)
  * [Dependency resolution in the browser](#dependency-resolution-in-the-browser)
  * [Compression and bundling depending](#compression-and-bundling-depending-on-the-running-environment)
  * [Caching to avoid unnecessary roundtrips](#caching-to-avoid-unnecessary-roundtrips)
  * [Clear cache on new releases](#clear-cache-on-new-releases)
* [Goals](#goals)
* [Maven plugin](#maven-plugin)
* [Spring integration](#spring-integration)

## Background
[WebJars](http://www.webjars.org/) makes a great effort to solve the problem of
JavaScript libraries distribution. However, there're another problems related
to configuration management on the application side. The following list covers
common issues when a web application is dealing with JavaScript libraries:

* Dependency resolution in the browser
* Compression and bundling depending on the running environment
* Caching to avoid unnecessary roundtrips
* Clear cache on new releases

### Dependency resolution in the browser
Some libraries as [RequireJS](http://www.requirejs.org/) tries to solve this
issue on the client side. Maybe some small-medium applications or single-page applications could find it useful, but in highly modular and distributed applications with a growing development team these libraries quickly reach their limits.

The only reasonable way out is to resolve dependencies on the server-side.
There're another bunch of libraries which do this task, but they require a lot
of configuration to map the application dependency tree, and finally it makes
them quite unpractical for the everyday life.

WebJars solved the half of this issue: dependencies between libraries are
resolved, but dependencies within libraries aren't. Most of libraries support
extensions that are included in the distribution package and they depends on
the core files. Rich component libraries are the most famous case of this kind
of libraries. As dependencies are at file-level, there colud not be a magic
generic algorithm to detect dependencies, so dependency mapping must be
done per-file. Of course, not ALL files depends on another ones, but it is
enough common particularly in modular libraries and applications.

### Compression and bundling depending on the running environment
Performance matters. But what is performance? From the end user perspective (and
all in web development is about people) performance is how many time they have
to wait after each click.

Compression and bundling, but mainly bundling, make a huge difference in the
perceived load time. Bundling avoids the client-side development Nemesis:
roadtrips to the server. Only with bundling and adding gzip support to the web
server the performance is almost saved.

Compression is another long-term discussion that I would like to take off from
the scope of this document. Some of them are technical (i.e: browser processing)
much of them not (i.e.: freedom and security).

### Caching to avoid unnecessary roundtrips
Another step in the road to make users happy is to choose a policy for
caching static resources. Caching avoids more roundtrips to the server, and it
works as a contract between [the browser and web servers](http://www.mnot.net/cache_docs/).

When a web application serves static content it must set the proper HTTP headers
according to the chosen cache strategy. Most Java frameworks for web application
development already have built-in caching support.

It is also important to enable/disable caching depending on the running
environment. For instance, caching could be extremely painful during
development.

### Clear cache on new releases
Finally, new application releases may have an eviction policy to force browsers
to reload cached resources. Sometimes application server restart is enough to
accomplish this task, but sometimes not. Anyway, I would like to take this
discussion away from the scope of this document.

## Goals
The goals of this project are:

* ~~Use webjars to resolve dependencies between libraries not only in configuration
management but also in application side.~~
* Provide a convention to map dependencies between files within a library
(hopefully integrated into webjars).
* Provide dependency resolution and bundling to applications.

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

