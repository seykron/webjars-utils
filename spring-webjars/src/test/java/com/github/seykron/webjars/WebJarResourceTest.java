package com.github.seykron.webjars;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.github.seykron.webjars.WebJarResource.MediaType;

/** Tests the {@link WebJarResource} class.
 */
public class WebJarResourceTest {

  private WebJarResource resource;

  private DependencyGraph dependencyGraph;

  private JSONObject dependencyDescriptor;

  @Before
  public void setUp() throws Exception {
    ClassLoader classLoader = new TestClassLoader();
    JSONArray jsonDependencyGraph = new JSONArray(IOUtils.toString(classLoader
        .getResource("com/github/seykron/webjars/deps.js")));
    dependencyGraph = new DependencyGraph(jsonDependencyGraph);

    for (int i = 0; i < jsonDependencyGraph.length(); i++) {
      JSONObject jsonDependency = jsonDependencyGraph.getJSONObject(i);
      if ("jasmine-jquery".equals(jsonDependency.getString("name"))) {
        dependencyDescriptor = jsonDependency;
        break;
      }
    }

    Thread.currentThread().setContextClassLoader(classLoader);
  }

  @Test
  public void newInstance() {
    resource = new WebJarResource(dependencyGraph,
        dependencyDescriptor, MediaType.JS);
    assertThat(resource.getId(), is(dependencyDescriptor.getString("id")));
    assertThat(resource.getName(), is(dependencyDescriptor.getString("name")));
    assertThat(resource.getVersion(),
        is(dependencyDescriptor.getString("version")));
    assertThat(resource.getType(), is(MediaType.JS));
    assertThat(resource.getFilename(),
        is(dependencyDescriptor.getString("name") + ".js"));
    assertThat(resource.getDependencies().size(), is(2));
  }

  @Test
  public void getInputStream() throws IOException {
    resource = new WebJarResource(dependencyGraph,
        dependencyDescriptor, MediaType.JS);
    InputStream in = resource.getInputStream();
    assertThat(IOUtils.toString(in),
        is(StringUtils.repeat(TestClassLoader.TEST_DATA, 5)));
    in.close();
  }
}
