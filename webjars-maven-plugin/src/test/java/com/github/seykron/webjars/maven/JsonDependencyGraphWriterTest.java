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
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.maven.model.Dependency;
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
    final String[] entriesJasmine = new String[] {
        "/jasmine/foo.js", "/jasmine/foo.css", "/jasmine/foo.txt"
    };
    final String[] entriesJasmineJquery = new String[] {
        "/jasmine-jquery/foo.js", "/jasmine-jquery/foo.css",
        "/jasmine-jquery/foo.txt"
    };
    final String[] entriesJasmineReporters = new String[] {
        "/jasmine-reporters/foo.js", "/jasmine-reporters/foo.css",
        "/jasmine-reporters/foo.txt"
    };
    final String[] entriesJquery = new String[] {
        "/jquery/foo.js", "/jquery/foo.css", "/jquery/foo.txt"
    };

    DependencyInfo jasmine = createDependencyInfo(
        "org.webjars:jasmine:jar:1.3.1", entriesJasmine);
    DependencyInfo jasmineJquery = createDependencyInfo(
        "org.webjars:jasmine-jquery:jar:1.4.2", entriesJasmineJquery);
    DependencyInfo jasmineReporters = createDependencyInfo(
        "org.webjars:jasmine-reporters:jar:0.2.1", entriesJasmineReporters);
    DependencyInfo jquery = createDependencyInfo("org.webjars:jquery:jar:1.6.2",
        entriesJquery);

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

    JSONObject result = new JSONObject(out.toString());
    JSONObject jsonTable = result.getJSONObject("table");
    JSONObject jsonIndex = result.getJSONObject("index");

    JSONObject jsonDependency = jsonTable
        .getJSONObject("org.webjars:jasmine-jquery:jar:1.4.2");

    assertThat(jsonDependency.getString("id"),
        is("org.webjars:jasmine-jquery:jar:1.4.2"));
    assertThat(jsonDependency.getString("name"),
        is("jasmine-jquery"));
    assertThat(jsonDependency.getString("version"),
        is("1.4.2"));
    assertThat(jsonDependency.getJSONArray("js").length(), is(1));
    assertThat(jsonDependency.getJSONArray("css").length(), is(1));
    assertThat(jsonDependency.getJSONArray("dependencies").length(), is(2));
    assertThat(jsonDependency.getJSONArray("dependencies").getString(0),
        is("org.webjars:jasmine:jar:1.3.1"));
    assertThat(jsonDependency.getJSONArray("dependencies").getString(1),
        is("org.webjars:jquery:jar:1.6.2"));

    assertThat(jsonIndex.getString("/jquery/foo.js"),
        is("org.webjars:jquery:jar:1.6.2"));
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
