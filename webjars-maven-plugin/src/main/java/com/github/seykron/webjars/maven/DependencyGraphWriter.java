package com.github.seykron.webjars.maven;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/** Writes a dependency graph to a specific format.
 */
public abstract class DependencyGraphWriter {

  /** Dependency graph to write, it's never null. */
  private Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;

  /** Writes the dependency graph using the specified writer.
   *
   * @param theDependencyGraph Dependency graph to write. Cannot be null.
   * @param writer Writer to write dependency. Cannot be null.
   * @throws IOException if the graph cannot be written.
   */
  protected abstract void write(
      final Map<DependencyInfo, List<DependencyInfo>> theDependencyGraph,
      final Writer writer) throws IOException;

  /** Constructs a dependency writer and sets the graph to write.
   *
   * @param theDependencyGraph Dependency graph to write. Cannot be null.
   */
  public DependencyGraphWriter(
      final Map<DependencyInfo, List<DependencyInfo>> theDependencyGraph) {
    Validate.notNull(theDependencyGraph,
        "The dependency graph cannot be null.");
    dependencyGraph = theDependencyGraph;
  }

  /** Writes the dependency graph using the specified writer. It closes the
   * writer.
   *
   * @param writer Writer to write the dependency graph. Cannot be null.
   */
  public void write(final Writer writer) {
    Validate.notNull(writer, "The writer cannot be null.");

    try {
      write(dependencyGraph, writer);
      writer.close();
    } catch (Exception cause) {
      throw new RuntimeException("Cannot write dependency graph.", cause);
    }
  }
}
