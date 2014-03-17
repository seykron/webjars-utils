package com.github.seykron.webjars;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import com.github.seykron.webjars.WebJarResource.MediaType;

/** Tests the {@link DependencyGraph} class.
 */
public class DependencyGraphTest {

  private DependencyGraph dependencyGraph;

  @Before
  public void setUp() throws Exception {
    ClassLoader classLoader = new TestClassLoader();
    JSONArray jsonDependencyGraph = new JSONArray(IOUtils.toString(classLoader
        .getResource("com/github/seykron/webjars/deps.js")));
    dependencyGraph = new DependencyGraph(jsonDependencyGraph);

    Thread.currentThread().setContextClassLoader(classLoader);
  }

  @Test
  public void findDependencyById() {
    WebJarResource resource = dependencyGraph.findDependencyById(
        "org.webjars:jasmine-jquery:jar:1.4.2", MediaType.JS);
    assertThat(resource.getName(), is("jasmine-jquery"));
  }

  @Test
  public void findDependencyByPath() {
    WebJarResource resource = dependencyGraph.findDependencyByPath(
        "/webjars/jasmine-jquery/1.4.2/jasmine-jquery.js", MediaType.JS);
    assertThat(resource.getName(), is("jasmine-jquery"));
  }
}
