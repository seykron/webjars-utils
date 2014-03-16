package com.github.seykron.webjars.maven;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.easymock.Capture;
import org.junit.Test;


/** Tests the {@link DependencyGraphBuilder} class.
 */
public class DependencyGraphBuilderTest {

  private DependencyGraphBuilder graphBuilder;

  @Test
  public void create() throws Exception {
    File artifactFile = File.createTempFile("foo", "bar");

    try {
      Artifact artifactToResolve = createMock(Artifact.class);
      ArtifactFilter filter = createMock(ArtifactFilter.class);
      expect(artifactToResolve.getGroupId()).andReturn("org.webjars");
      expect(artifactToResolve.getId())
        .andReturn("org.webjars:jasmine-jquery:jar:1.4.2");
      expect(artifactToResolve.getArtifactId())
        .andReturn("org.webjars:jasmine-jquery:jar:1.4.2").times(4);
      expect(artifactToResolve.getFile()).andReturn(artifactFile);
      expect(artifactToResolve.getDependencyFilter()).andReturn(filter);
      replay(artifactToResolve);

      MavenProject project = createMock(MavenProject.class);
      Artifact parentArtifact = createMock(Artifact.class);
      List<String> remoteRepos = new ArrayList<String>();
      expect(project.getDependencyArtifacts())
        .andReturn(new HashSet<Artifact>(Arrays.asList(artifactToResolve)));
      expect(project.getRemoteArtifactRepositories()).andReturn(remoteRepos);
      expect(project.getArtifact()).andReturn(parentArtifact);
      replay(project);

      ArtifactMetadataSource metadataSource =
          createMock(ArtifactMetadataSource.class);
      ArtifactRepository localRepository = createMock(ArtifactRepository.class);

      Artifact resolvedDependency = createMock(Artifact.class);
      expect(resolvedDependency.getId())
        .andReturn("org.webjars:jasmine:jar:1.3.1");
      expect(resolvedDependency.getArtifactId())
        .andReturn("org.webjars:jasmine:jar:1.3.1").times(4);
      expect(resolvedDependency.getFile()).andReturn(artifactFile);
      replay(resolvedDependency);

      Set<Artifact> resolvedArtifacts = new HashSet<Artifact>(
          Arrays.asList(resolvedDependency));
      ArtifactResolutionResult resolutionResult =
          createMock(ArtifactResolutionResult.class);
      expect(resolutionResult.getArtifacts()).andReturn(resolvedArtifacts);
      replay(resolutionResult);

      ArtifactResolver artifactResolver = createMock(ArtifactResolver.class);
      Capture<Set<?>> capturedArtifacts = new Capture<Set<?>>();
      expect(artifactResolver.resolveTransitively(capture(capturedArtifacts),
          eq(parentArtifact), eq(localRepository), eq(remoteRepos),
          eq(metadataSource), eq(filter))).andReturn(resolutionResult);
      replay(artifactResolver);

      graphBuilder = new DependencyGraphBuilder(artifactResolver,
          metadataSource, localRepository, project) {
        @Override
        DependencyInfo createDependencyInfo(final Dependency dependency) {
          return createMockDependencyInfo(dependency.getArtifactId());
        }
      };

      Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;
      dependencyGraph = graphBuilder.create();

      assertThat(dependencyGraph.size(), is(1));

      verify(artifactResolver, project, artifactToResolve, resolvedDependency,
          resolutionResult);
    } finally {
      artifactFile.delete();
    }
  }

  private DependencyInfo createMockDependencyInfo(final String id) {
    final MockJarFile mockJarFile = new MockJarFile(new String[] {});
    Dependency dependency = new Dependency();
    dependency.setArtifactId(id);

    DependencyInfo dependencyInfo = new DependencyInfo(dependency) {
      @Override
      JarFile getJarFile() throws IOException {
        return mockJarFile.getJarFile();
      }
    };
    return dependencyInfo;
  }
}
