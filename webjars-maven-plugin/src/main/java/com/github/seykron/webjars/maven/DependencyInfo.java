package com.github.seykron.webjars.maven;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.maven.model.Dependency;

/** Extracts files related to a dependency.
 */
public class DependencyInfo {

  /** Pattern to parse artifact information from the id.
   * i.e.: org.webjars:jasmine:jar:1.3.1
   */
  private static final Pattern INFO_PATTERN =
      Pattern.compile("^.+:(.+):.+:(.+)$");

  /** Dependency to extract files from, it's never null.  */
  private final Dependency dependency;

  /** List of CSS files in this dependency, it's never null. */
  private final List<String> cssFiles = new LinkedList<String>();

  /** List of javascript files in this dependency, it's never null.. */
  private final List<String> jsFiles = new LinkedList<String>();

  /** Dependency name, it's never null or empty. */
  private String name;

  /** Dependency version, it's never null or empty. */
  private String version;

  /** Creates a reader for the specified dependency.
   *
   * @param theDependency Dependency to read. Cannot be null.
   */
  public DependencyInfo(final Dependency theDependency) {
    Validate.notNull(theDependency, "The dependency cannot be null.");
    dependency = theDependency;

    readFiles();
    parseInfo();
  }

  /** Returns the dependency unique id.
   * @return A valid id, never null or empty.
   */
  public String getId() {
    return dependency.getArtifactId();
  }

  /** Returns the dependency name.
   * @return A valid name, never null or empty.
   */
  public String getName() {
    return name;
  }

  /** Returns the dependency version.
   * @return A valid version, never null or empty.
   */
  public String getVersion() {
    return version;
  }

  /** Returns the list of CSS files in this dependency.
   * @return A valid list of files, never null.
   */
  public List<String> getCssFiles() {
    return cssFiles;
  }

  /** Returns the list of JavaScript files in this dependency.
   * @return A valid list of files, never null.
   */
  public List<String> getJsFiles() {
    return jsFiles;
  }

  /** Returns the dependency jar file.
   *
   * @return A valid JAR file, never null.
   * @throws IOException if the file cannot be opened.
   */
  JarFile getJarFile() throws IOException {
    return new JarFile(dependency.getSystemPath());
  }

  /** Reads the dependency file and extracts containing files.
   */
  private void readFiles() {
    JarFile jarFile = null;

    try {
      jarFile = getJarFile();

      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        if (jarEntry.getName().toLowerCase().endsWith(".css")) {
          cssFiles.add(jarEntry.getName());
        } else if (jarEntry.getName().toLowerCase().endsWith(".js")) {
          jsFiles.add(jarEntry.getName());
        }
      }
    } catch (IOException cause) {
      throw new RuntimeException("Cannot extract files from dependency", cause);
    } finally {
      if (jarFile != null) {
        try {
          jarFile.close();
        } catch (IOException cause) {
          throw new RuntimeException(cause);
        }
      }
    }
  }

  /** Parses dependency artifact information.
   */
  private void parseInfo() {
    Matcher matcher = INFO_PATTERN.matcher(dependency.getArtifactId());

    if (matcher.matches()) {
      name = matcher.group(1);
      version = matcher.group(2);
    } else {
      throw new RuntimeException("Cannot parse artifact info: "
          + dependency.getArtifactId());
    }
  }
}
