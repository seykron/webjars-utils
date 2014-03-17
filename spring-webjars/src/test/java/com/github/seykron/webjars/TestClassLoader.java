package com.github.seykron.webjars;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Class loader that returns {@link #TEST_DATA} each time a webjar resource
 * is required.
 */
public class TestClassLoader extends ClassLoader {

  /** Data to retrieve when a webjar is required. */
  public static final String TEST_DATA = "function () {}";

  /** Default constructor, sets the thread context class loader as parent.
   */
  public TestClassLoader() {
    super(Thread.currentThread().getContextClassLoader());
  }

  /** Returns an input stream to read {@link #TEST_DATA} every time a webjar
   * resource is required.
   *
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
    if (name != null && name.startsWith("META-INF/resources/webjars/")) {
      return new ByteArrayInputStream(TEST_DATA.getBytes());
    } else {
      return super.getResourceAsStream(name);
    }
  }
}
