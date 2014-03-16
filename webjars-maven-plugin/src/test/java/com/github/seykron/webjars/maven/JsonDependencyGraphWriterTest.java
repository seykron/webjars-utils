package com.github.seykron.webjars.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarFile;

import org.apache.maven.model.Dependency;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


/** Tests the {@link JsonDependencyGraphWriter} class.
 */
public class JsonDependencyGraphWriterTest {

  private Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;

  private JsonDependencyGraphWriter dependencyWriter;

  @Before
  public void setUp() {
    final String[] entries = new String[] { "foo.js", "foo.css", "foo.txt" };

    DependencyInfo jasmine = createDependencyInfo(
        "org.webjars:jasmine:jar:1.3.1", entries);
    DependencyInfo jasmineJquery = createDependencyInfo(
        "org.webjars:jasmine-jquery:jar:1.4.2", entries);
    DependencyInfo jasmineReporters = createDependencyInfo(
        "org.webjars:jasmine-reporters:jar:0.2.1", entries);
    DependencyInfo jquery = createDependencyInfo("org.webjars:jquery:jar:1.6.2",
        entries);

    dependencyGraph = new HashMap<DependencyInfo, List<DependencyInfo>>();
    dependencyGraph.put(jasmine, new ArrayList<DependencyInfo>());
    dependencyGraph.put(jasmineReporters, Arrays.asList(jasmine));
    dependencyGraph.put(jasmineJquery, Arrays.asList(jasmine, jquery));
    dependencyGraph.put(jquery, new ArrayList<DependencyInfo>());
  }

  @Test
  public void write() {
    dependencyWriter = new JsonDependencyGraphWriter(dependencyGraph);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Writer writer = new OutputStreamWriter(out);

    dependencyWriter.write(writer);

    JSONArray jsonGraph = new JSONArray();
    Iterator<Entry<DependencyInfo, List<DependencyInfo>>> it;
    it = dependencyGraph.entrySet().iterator();

    while (it.hasNext()) {
      Entry<DependencyInfo, List<DependencyInfo>> entry = it.next();
      DependencyInfo parent = entry.getKey();
      List<DependencyInfo> dependencies = entry.getValue();

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

      jsonGraph.put(jsonDependency);
    }

    assertThat(out.toString(), is(jsonGraph.toString()));
  }

  private DependencyInfo createDependencyInfo(final String id,
      final String[] entries) {
    final MockJarFile mockJarFile = new MockJarFile(entries);
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
