package com.github.seykron.webjars.maven;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Test;


/** Tests the {@link DependenciesMojo} class.
 */
public class DependenciesMojoTest {

  @Test
  public void execute() throws Exception {
    Map<DependencyInfo, List<DependencyInfo>> dependencyGraph;
    dependencyGraph = new HashMap<DependencyInfo, List<DependencyInfo>>();

    final DependencyGraphBuilder graphBuilder =
        createMock(DependencyGraphBuilder.class);
    expect(graphBuilder.create()).andReturn(dependencyGraph);
    replay(graphBuilder);

    final DependencyGraphWriter writer =
        createMock(DependencyGraphWriter.class);
    writer.write(isA(FileWriter.class));
    replay(writer);

    DependenciesMojo mojo = new DependenciesMojo() {
      @Override
      DependencyGraphBuilder createGraphBuilder() {
        return graphBuilder;
      }

      @Override
      protected DependencyGraphWriter createWriter(
          final Map<DependencyInfo, List<DependencyInfo>> dependencyGraph) {
        return writer;
      }
    };

    File outputFile = File.createTempFile("foo", "bar");
    ReflectionUtils.setVariableValueInObject(mojo, "outputFile", outputFile);

    try {
      mojo.execute();

      verify(graphBuilder, writer);
    } finally {
      outputFile.delete();
    }
  }
}
