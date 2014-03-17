package com.github.seykron.webjars;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.seykron.webjars.WebJarResource.MediaType;

/** Represents a WebJar dependency graph. It assumes default JSON-format.
 */
public class DependencyGraph {

  /** Dependency graph as JSON, it's never null. */
  private final JSONArray dependencyGraph;

  /** Creates a dependency graph and sets the JSON data.
   *
   * @param theDependencyGraph Dependency graph as JSON. Cannot be null.
   */
  public DependencyGraph(final JSONArray theDependencyGraph) {
    Validate.notNull(theDependencyGraph,
        "The dependency graph cannot be null.");
    dependencyGraph = theDependencyGraph;
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

    for (int i = 0; i < dependencyGraph.length(); i++) {
      JSONObject dependency = (JSONObject) dependencyGraph.get(i);

      if (dependencyId.equalsIgnoreCase(dependency.getString("id"))) {
        return new WebJarResource(this, dependency, type);
      }
    }
    return null;
  }

  /** Finds a dependency from its path according to WebJars specification.
   *
   * @param thePath Required webjar path. Cannot be null or empty.
   * @param type Type of resource. Cannot be null.
   * @return The required dependency, or null if it does not exist.
   */
  public WebJarResource findDependencyByPath(final String thePath,
      final MediaType type) {
    Validate.notEmpty(thePath, "The dependency path cannot be null.");

    String url = thePath;

    if (url.startsWith("/")) {
      url = url.substring(1);
    }

    List<String> pathAttrib = Arrays.asList(url.split("/"));
    if (!"webjars".equalsIgnoreCase(pathAttrib.get(0))) {
      return null;
    }

    String name = pathAttrib.get(1);
    String version = pathAttrib.get(2);
    String dependencyId = "org.webjars:" + name + ":jar:" + version;

    return findDependencyById(dependencyId, type);
  }
}
