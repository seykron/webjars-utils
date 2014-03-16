package com.github.seykron.webjars.maven;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

/** Builds the Webjars dependency graph.
 */
public class DependencyGraphBuilder {

  /** Webjars maven group id. */
  private static final String WEBJARS_GROUP_ID = "org.webjars";

  /** Dependencies resolver; it's never null.
   */
  private ArtifactResolver artifactResolver;

  /** Maven local repository; it's never null. */
  private ArtifactRepository localRepository;

  /** Provides some metadata operations, like querying the remote repository
   * for a list of versions available for an artifact. It's never null.
   */
  private ArtifactMetadataSource metadataSource;

  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project; it's never null.
   */
  private MavenProject project;

  /** Cache for dependencies, it's never null. It's cleared when a new
   * dependency graph is created. */
  private Map<String, DependencyInfo> dependencyCache =
      new HashMap<String, DependencyInfo>();

  /** Default constructor for testing purposes, do not use.
   */
  DependencyGraphBuilder() {
  }

  /** Creates a new maven class loader builder.
   *
   * @param theArtifactResolver Resolver to download dependencies. Cannot be
   *    null.
   * @param theMetadataSource Provides artifacts metadata. Cannot be null.
   * @param theLocalRepository Maven local repository. Cannot be null.
   * @param theProject The reference maven project. Cannot be null.
   */
  public DependencyGraphBuilder(final ArtifactResolver theArtifactResolver,
      final ArtifactMetadataSource theMetadataSource,
      final ArtifactRepository theLocalRepository,
      final MavenProject theProject) {
    Validate.notNull(theArtifactResolver,
        "The artifact resolver cannot be null.");
    Validate.notNull(theMetadataSource,
        "The graph builder cannot be null.");
    Validate.notNull(theLocalRepository,
        "The local repository cannot be null.");
    Validate.notNull(theProject,
        "The maven project cannot be null.");

    artifactResolver = theArtifactResolver;
    metadataSource = theMetadataSource;
    project = theProject;
    localRepository = theLocalRepository;
  }

  /** Builds the class loader using the current configuration.
   * @return Returns a valid class loader. Never returns null.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<DependencyInfo, List<DependencyInfo>> create() {
    Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;

    dependencyGraph = new HashMap<DependencyInfo, List<DependencyInfo>>();

    try {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      artifacts.addAll(project.getDependencyArtifacts());

      for (Artifact artifact : artifacts) {
        if (artifact.getGroupId().equalsIgnoreCase(WEBJARS_GROUP_ID)) {
          Set artifactsToResolve = new HashSet(Arrays.asList(artifact));
          ArtifactResolutionResult result;

          result = artifactResolver.resolveTransitively(artifactsToResolve,
              project.getArtifact(), localRepository,
              project.getRemoteArtifactRepositories(), metadataSource,
              artifact.getDependencyFilter());

          List<DependencyInfo> dependencies = new LinkedList<DependencyInfo>();

          for (Object dependencyArtifactObj : result.getArtifacts()) {
            Artifact dependencyArtifact = (Artifact) dependencyArtifactObj;

            if (!dependencyArtifact.getArtifactId()
                .equals(artifact.getArtifactId())) {
              dependencies.add(getDependency(dependencyArtifact));
            }
          }

          dependencyGraph.put(getDependency(artifact), dependencies);
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException("Cannot resolve the artifact.", ex);
    } finally {
      dependencyCache.clear();
    }

    return dependencyGraph;
  }

  /** Retrieves the dependency for the specified artifact from the cache or
   * creates it if it does not exist.
   *
   * @param artifact Artifact related to the required dependency.
   *    Cannot be null.
   * @return Returns the required dependency, never null.
   */
  private DependencyInfo getDependency(final Artifact artifact) {
    if (!dependencyCache.containsKey(artifact.getArtifactId())) {
      Dependency dependency = new Dependency();
      dependency.setArtifactId(artifact.getId());
      // WARNING: may be deprecated.
      dependency.setSystemPath(artifact.getFile().getAbsolutePath());

      DependencyInfo info = createDependencyInfo(dependency);

      dependencyCache.put(artifact.getArtifactId(), info);
    }
    return dependencyCache.get(artifact.getArtifactId());
  }

  /** Creates the dependency information for the specified dependency.
   * @param dependency Dependency to resolve. Cannot be null.
   * @return A valid info, never null.
   */
  DependencyInfo createDependencyInfo(final Dependency dependency) {
    return new DependencyInfo(dependency);
  }
}
