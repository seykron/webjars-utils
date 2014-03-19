package com.github.seykron.webjars;

import org.apache.commons.lang.Validate;
import org.json.JSONObject;

import com.github.seykron.webjars.WebJarResource.MediaType;

/** Represents a WebJar dependency graph. It assumes default JSON-format.
 */
public class DependencyGraph {

  /** Index to lookup dependencies from files, it's never null. */
  private final JSONObject index;

  /** Table with mappings from dependency id to dependency, it's never null. */
  private final JSONObject table;

  /** Creates a dependency graph and sets the JSON data.
   *
   * @param theDependencyGraph Dependency graph as JSON. Cannot be null.
   */
  public DependencyGraph(final JSONObject theDependencyGraph) {
    Validate.notNull(theDependencyGraph,
        "The dependency graph cannot be null.");
    index = theDependencyGraph.getJSONObject("index");
    table = theDependencyGraph.getJSONObject("table");
  }

  /** Searches for the specified dependency in the graph.
   *
   * @param dependencyId Id of the requried dependency. Cannot be null or empty.
   * @param type Type of resource. Cannot be null.
   * @return The required dependency as a {@link WebJarResource}, or null if it
   *    does not exist.
   */
  public WebJarResource findDependencyById(final String dependencyId,
      final MediaType type) {
    Validate.notEmpty(dependencyId,
        "The dependency id cannot be null or empty.");
    Validate.notNull(type, "The resource type cannot be null.");

    WebJarResource resource = null;
    JSONObject dependency = table.getJSONObject(dependencyId);

    if (dependencyId.equalsIgnoreCase(dependency.getString("id"))) {
      resource = new WebJarResource(this, dependency, type);
    }

    return resource;
  }

  /** Finds a dependency from a file path. It uses the following format:
   * <pre>
   * /webjars/${artifact-name}/${artifact-version}/${file-name}
   * </pre>
   *
   * @param thePath Required webjar path. Cannot be null or empty.
   * @param type Type of resource. Cannot be null.
   * @return The required dependency, or null if it does not exist.
   */
  public WebJarResource findDependencyByPath(final String thePath,
      final MediaType type) {
    Validate.notEmpty(thePath, "The dependency path cannot be null or empty.");

    String path = thePath;

    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    path = "META-INF/resources/" + path;

    Validate.isTrue(index.has(path), "File not found: " + thePath);

    return findDependencyById(index.getString(path), type);
  }
}
