package com.github.seykron.webjars.maven;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.maven.model.Dependency;
import org.junit.Test;


/** Tests the {@link DependencyInfo} class.
 */
public class DependencyInfoTest {

  private MockJarFile mockJarFile;

  @Test
  public void newInstance() throws IOException {
    Dependency dependency = new Dependency();
    dependency.setArtifactId("org.webjars:jquery:jar:1.6.2");

    final String[] entries = new String[] { "foo.js", "foo.css", "foo.txt" };

    mockJarFile = new MockJarFile(entries);

    DependencyInfo dependencyInfo = new DependencyInfo(dependency) {
      @Override
      JarFile getJarFile() throws IOException {
        return mockJarFile.getJarFile();
      }
    };

    assertThat(dependencyInfo.getId(), is("org.webjars:jquery:jar:1.6.2"));
    assertThat(dependencyInfo.getName(), is("jquery"));
    assertThat(dependencyInfo.getVersion(), is("1.6.2"));
    assertThat(dependencyInfo.getCssFiles().size(), is(1));
    assertThat(dependencyInfo.getCssFiles().get(0), is("foo.css"));
    assertThat(dependencyInfo.getJsFiles().size(), is(1));
    assertThat(dependencyInfo.getJsFiles().get(0), is("foo.js"));

    verify(mockJarFile.getJarFile());
  }
}
