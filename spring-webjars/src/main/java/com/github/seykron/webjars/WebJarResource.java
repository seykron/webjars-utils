package com.github.seykron.webjars;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/** Resource to load files from WebJars.
 * <p>
 * It uses the thread's class loader to retrieve resources from the classpath.
 * </p>
 */
public class WebJarResource extends AbstractResource {

  /** Dependency graph this resource belongs to, it's never null. */
  private final DependencyGraph dependencyGraph;

  /** Dependency unique id, it's never null or empty. */
  private final String id;

  /** Resource name, it's never null or empty. */
  private final String name;

  /** Dependency version, it's never null or empty. */
  private final String version;

  /** List of JavaScript files in this dependency, it's never null. */
  private final List<String> jsFiles;

  /** List of CSS files in this dependency, it's never null. */
  private final List<String> cssFiles;

  /** Resource dependencies, it's never null. */
  private final List<WebJarResource> dependencies =
      new LinkedList<WebJarResource>();

  /** Type of resource, it's never null. */
  private final MediaType type;

  /** Creates a web jar resource and sets the related descriptor.
   * @param theDependencyGraph Graph with resolved webjar dependencies. Cannot
   *    be null.
   * @param resourceDescriptor Descriptor of the dependency represented by
   *    this resource. Cannot be null.
   * @param theType Type of resource. Cannot be null.
   */
  WebJarResource(final DependencyGraph theDependencyGraph,
      final JSONObject resourceDescriptor,
      final MediaType theType) {
    Validate.notNull(theDependencyGraph,
        "The dependency graph cannot be null.");
    Validate.notNull(resourceDescriptor,
        "The resource descriptor cannot be null.");
    Validate.notNull(theType, "The resource type cannot be null.");

    dependencyGraph = theDependencyGraph;

    id = resourceDescriptor.getString("id");
    name = resourceDescriptor.getString("name");
    version = resourceDescriptor.getString("version");
    jsFiles = asList(resourceDescriptor.getJSONArray("js"));
    cssFiles = asList(resourceDescriptor.getJSONArray("css"));
    type = theType;

    JSONArray jsonDependencies = resourceDescriptor
        .getJSONArray("dependencies");
    for (int i = 0; i < jsonDependencies.length(); i++) {
      WebJarResource dependency = dependencyGraph
          .findDependencyById(jsonDependencies.getString(i), type);

      dependencies.add(dependency);
    }
  }

  /** {@inheritDoc}.
   */
  @Override
  public String getDescription() {
    return "Resource to load files from WebJars";
  }

  /** {@inheritDoc}.
   */
  @Override
  public InputStream getInputStream() throws IOException {
    StringBuilder buffer = new StringBuilder();

    for (WebJarResource dependency : dependencies) {
      buffer.append(read(dependency));
    }

    for (String file : jsFiles) {
      Resource resource = loadResource(file);
      buffer.append(read(resource));
    }

    return new ByteArrayInputStream(buffer.toString().getBytes());
  }

  /** Returns the resource unique id.
   * @return A valid id, never null or empty.
   */
  public String getId() {
    return id;
  }

  /** Returns the resource name.
   * @return A valid name, never null or empty.
   */
  public String getName() {
    return name;
  }

  /** Returns the resource version.
   * @return A valid version, never null or empty.
   */
  public String getVersion() {
    return version;
  }

  /** Returns an unmodifiable list of JavaScript files included in this
   * resource.
   *
   * @return A valid list of files, never null.
   */
  public List<String> getJsFiles() {
    return Collections.unmodifiableList(jsFiles);
  }

  /** Returns an unmodifiable list of CSS files included in this resource.
   *
   * @return A valid list of files, never null.
   */
  public List<String> getCssFiles() {
    return Collections.unmodifiableList(cssFiles);
  }

  /** Returns an unmodifiable list of dependencies for this resource.
   *
   * @return A valid list of dependencies, never null.
   */
  public List<WebJarResource> getDependencies() {
    return Collections.unmodifiableList(dependencies);
  }

  /** Returns the resource type.
   * @return A valid resource type, never null.
   */
  public MediaType getType() {
    return type;
  }

  /** Returns the dependency name and extension.
   * @return A valid filename, never null or empty.
   * @throws IllegalStateException if name cannot be resolved.
   */
  @Override
  public String getFilename() throws IllegalStateException {
    return name + "." + type.name().toLowerCase();
  }

  /** {@inheritDoc}
   */
  @Override
  protected File getFileForLastModifiedCheck() throws IOException {
    // TODO(seykron): implement eviction strategy.
    File file = new File(".");
    file.setLastModified(System.currentTimeMillis());

    return file;
  }

  /** Converts a {@link JSONArray} to a list of strings.
   *
   * @param theArray Array to convert. Cannot be null.
   * @return The provided array as a list of Strings, never null.
   */
  private List<String> asList(final JSONArray theArray) {
    List<String> result = new LinkedList<String>();

    for (int i = 0; i < theArray.length(); i++) {
      result.add(theArray.getString(i));
    }

    return result;
  }

  /** Loads a resource from the specified classpath.
   *
   * @param path Resource path. Cannot be null or empty.
   * @return A valid resource, never null.
   */
  Resource loadResource(final String path) {
    return new ClassPathResource(path,
        Thread.currentThread().getContextClassLoader());
  }

  /** Reads the specified resource and returns content as array of bytes.
   * @param resource Resource to read. Cannot be null.
   * @return The resource content, as bytes, never null.
   */
  private String read(final Resource resource) {
    InputStream in = null;

    try {
      in = resource.getInputStream();

      return IOUtils.toString(in);
    } catch (IOException cause) {
      throw new RuntimeException("Cannot read resource", cause);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /** List of supported webjars resources.
   */
  public enum MediaType {
    /** Javascript resource. */
    JS,

    /** CSS resource. */
    CSS
  }
}
