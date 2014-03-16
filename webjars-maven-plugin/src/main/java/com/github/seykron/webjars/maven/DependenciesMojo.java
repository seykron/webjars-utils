package com.github.seykron.webjars.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Builds the WebJars dependency tree using a
 * {@link JsonDependencyGraphWriter}.
 */
@Mojo(name = "build-dependencies",
  defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
  requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DependenciesMojo extends AbstractMojo {

  /** Maven's artifact resolver. */
  @Component
  private ArtifactResolver artifactResolver;

  /** Provides some metadata operations, like querying the remote repository for
   * a list of versions available for an artifact.
   */
  @Component
  private ArtifactMetadataSource metadataSource;

  /** Specifies the repository used for artifact handling.
   */
  @Parameter(defaultValue = "${localRepository}")
  private ArtifactRepository localRepository;

  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project.
   *
   * Injected by maven. This is never null.
   */
  @Component
  private MavenProject project;

  /** File to write dependencies graph, it's never null.
   */
  @Parameter(required = true)
  private File outputFile;

  /** {@inheritDoc}.
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      DependencyGraphBuilder builder = createGraphBuilder();
      Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;
      dependencyGraph = builder.create();

      DependencyGraphWriter writer = createWriter(dependencyGraph);
      Writer fileWriter = new FileWriter(outputFile);
      writer.write(fileWriter);
      fileWriter.close();
    } catch (IOException cause) {
      throw new MojoExecutionException("Error executing dependencies mojo",
          cause);
    }
  }

  /** Creates the writer to write the dependency graph, it is designed for
   * extension.
   *
   * @param dependencyGraph Dependency graph to write. Cannot be null.
   * @return The dependency writer, never null.
   */
  protected DependencyGraphWriter createWriter(
      final Map<DependencyInfo, List<DependencyInfo>> dependencyGraph) {
    return new JsonDependencyGraphWriter(dependencyGraph);
  }

  /** Creates the dependency graph builder.
   * @return A valid graph builder, never null.
   */
  DependencyGraphBuilder createGraphBuilder() {
    return new DependencyGraphBuilder(artifactResolver, metadataSource,
        localRepository, project);
  }
}
