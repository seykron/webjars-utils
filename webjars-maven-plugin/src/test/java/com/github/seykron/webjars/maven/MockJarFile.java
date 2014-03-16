package com.github.seykron.webjars.maven;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Mocks a {@link JarFile}.
 */
public class MockJarFile {

  /** Mocked jar file, it's never null. */
  private JarFile jarFile;

  /** Creates a mock jar file and sets the list of existing entries in the
   * file.
   * @param entries List of entries names. Cannot be null.
   */
  public MockJarFile(final String[] entries) {
    Enumeration<JarEntry> jarEntries = new Enumeration<JarEntry>() {
      private int index = -1;

      @Override
      public JarEntry nextElement() {
        index += 1;
        return new JarEntry(entries[index]);
      }

      @Override
      public boolean hasMoreElements() {
        return index < entries.length - 1;
      }
    };
    jarFile = createMock(JarFile.class);
    expect(jarFile.entries()).andReturn(jarEntries);

    try {
      jarFile.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    replay(jarFile);
  }

  /** Returns the mocked jar file.
   * @return a valid mock, never null.
   */
  public JarFile getJarFile() {
    return jarFile;
  }
}
