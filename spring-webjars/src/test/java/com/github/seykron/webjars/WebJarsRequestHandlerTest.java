package com.github.seykron.webjars;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

/** Tests the {@link WebJarsRequestHandler} class.
 */
public class WebJarsRequestHandlerTest {

  private WebJarsRequestHandler handler;

  @Before
  public void setUp() throws Exception {
    handler = new WebJarsRequestHandler("/com/github/seykron/webjars/deps.js");
    handler.afterPropertiesSet();
    Thread.currentThread().setContextClassLoader(new TestClassLoader());
  }

  @Test
  public void getResource() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
        "/webjars/jasmine-jquery/1.4.2/jasmine-jquery.js");
    Resource resource = handler.getResource(request);

    InputStream in = resource.getInputStream();

    assertThat(IOUtils.toString(in),
        is(StringUtils.repeat(TestClassLoader.TEST_DATA, 5)));
    in.close();
  }
}
