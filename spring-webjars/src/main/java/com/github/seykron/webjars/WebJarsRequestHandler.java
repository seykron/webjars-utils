package com.github.seykron.webjars;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.github.seykron.webjars.WebJarResource.MediaType;

/** Request handler that serves static content from WebJars.
 * <p>
 * It resolves dependencies if required.
 * </p>
 */
public class WebJarsRequestHandler extends ResourceHttpRequestHandler
  implements InitializingBean {

  /** Descriptor to read webjars dependencies, it's never null or empty. */
  private final String dependencyGraphDescriptor;

  /** Resolved dependency graph, it's never null after properties set. */
  private DependencyGraph dependencyGraph;

  /** Creates the request handler and sets the dependency graph descriptor.
   *
   * @param theDependencyGraphDescriptor Descriptor to read webjars
   *    dependencies. Must be a valid classpath resource. Cannot be null or
   *    empty.
   */
  public WebJarsRequestHandler(final String theDependencyGraphDescriptor) {
    Validate.notEmpty(theDependencyGraphDescriptor,
        "The dependency graph descriptor cannot be null or empty.");
    dependencyGraphDescriptor = theDependencyGraphDescriptor;
  }

  /** Reads the dependency graph descriptor.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    Resource resource = new ClassPathResource(dependencyGraphDescriptor);
    InputStream in = resource.getInputStream();

    try {
      dependencyGraph = new DependencyGraph(
          new JSONObject(IOUtils.toString(in)));
    } catch (Exception cause) {
      throw new RuntimeException("Cannot create dependency graph.", cause);
    } finally {
      in.close();
    }
  }

  /** Tries to resolve the required resource as a webjar dependency.
   * <p>
   * {@inheritDoc}
   * </p>
   */
  @Override
  protected Resource getResource(final HttpServletRequest request) {
    String path = (String) request.getAttribute(HandlerMapping
        .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    Validate.notEmpty(path, "Invalid request, not path info available.");

    String typeName = StringUtils.substringAfterLast(path, ".");
    Validate.notEmpty(typeName, "Unknown file type: " + path);

    MediaType type = MediaType.valueOf(typeName.toUpperCase());
    Resource result = dependencyGraph.findDependencyByPath(path, type);

    if (result == null) {
      result = super.getResource(request);
    }
    return result;
  }
}
