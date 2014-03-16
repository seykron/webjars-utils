package com.github.seykron.webjars.maven;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

/** Writes a dependency graph using a JSON structure.
 * <p>
 * Each dependency will have the following format:
 *   <pre>
 *   {
 *      id: 'org.webjars:jquery-ui:jar:1.8.0',
 *      name: 'jquery-ui',
 *      js: ['jquery.ui.js'],
 *      css: ['jquery.ui.css'],
 *      dependencies: ['org.webjars:jquery:jar:1.10.2']
 *    }
 *   </pre>
 * </p>
 */
public class JsonDependencyGraphWriter extends DependencyGraphWriter {

  /** Constructs a dependency writer and sets the graph to write.
   *
   * @param theDependencyGraph Dependency graph to write. Cannot be null.
   */
  public JsonDependencyGraphWriter(
      final Map<DependencyInfo, List<DependencyInfo>> theDependencyGraph) {
    super(theDependencyGraph);
  }

  /** Converts the dependency to JSON according to the format specified in the
   * class documentation.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  protected void write(
      final Map<DependencyInfo, List<DependencyInfo>> dependencyGraph,
      final Writer writer) throws IOException {

    Iterator<Entry<DependencyInfo, List<DependencyInfo>>> it;
    it = dependencyGraph.entrySet().iterator();

    writer.write("[");

    while (it.hasNext()) {
      Entry<DependencyInfo, List<DependencyInfo>> entry = it.next();

      writer.write(toJson(entry.getKey(), entry.getValue()));

      if (it.hasNext()) {
        writer.write(",");
      }
    }
    writer.write("]");
  }

  /** Converts the specified dependency to JSON.
   *
   * @param parent Dependency to convert. Cannot be null.
   * @param dependencies List of immediate dependencies. Cannot be null.
   * @return a valid JSON, never null or empty.
   */
  private String toJson(final DependencyInfo parent,
      final List<DependencyInfo> dependencies) {
    JSONObject jsonDependency = new JSONObject();

    jsonDependency.put("id", parent.getId());
    jsonDependency.put("name", parent.getName());
    jsonDependency.put("version", parent.getVersion());
    jsonDependency.put("css", new JSONArray(parent.getCssFiles()));
    jsonDependency.put("js", new JSONArray(parent.getJsFiles()));

    JSONArray jsonDependencies = new JSONArray();

    for (DependencyInfo dependency : dependencies) {
      jsonDependencies.put(dependency.getId());
    }
    jsonDependency.put("dependencies", jsonDependencies);
    return jsonDependency.toString();
  }
}
