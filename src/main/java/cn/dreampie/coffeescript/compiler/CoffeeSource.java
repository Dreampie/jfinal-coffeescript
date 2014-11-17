package cn.dreampie.coffeescript.compiler;

import cn.dreampie.resource.FileResource;
import cn.dreampie.resource.HttpResource;
import cn.dreampie.resource.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;

/**
 * Created by wangrenhui on 2014/7/11.
 */
public class CoffeeSource {

  private static Logger logger = LoggerFactory.getLogger(CoffeeSource.class);

  private Resource resource;
  private String content;
  private String normalizedContent;

  /**
   * Constructs a new <code>CoffeeSource</code>.
   * <p>
   * This will read the metadata and content of the LESS source, and will automatically resolve the imports.
   * </p>
   * <p>
   * The resource is read using the default Charset of the platform
   * </p>
   *
   * @param resource The <code>File</code> reference to the LESS source to read.
   * @throws java.io.FileNotFoundException If the LESS source (or one of its imports) could not be found.
   * @throws java.io.IOException           If the LESS source cannot be read.
   */
  public CoffeeSource(Resource resource) throws IOException {
    this(resource, Charset.defaultCharset());
  }

  /**
   * Constructs a new <code>CoffeeSource</code>.
   * <p>
   * This will read the metadata and content of the LESS resource, and will automatically resolve the imports.
   * </p>
   *
   * @param resource The <code>File</code> reference to the LESS resource to read.
   * @param charset  charset used to read the less resource.
   * @throws java.io.FileNotFoundException If the LESS resource (or one of its imports) could not be found.
   * @throws java.io.IOException           If the LESS resource cannot be read.
   */
  public CoffeeSource(Resource resource, Charset charset) throws IOException {
    if (resource == null) {
      throw new IllegalArgumentException("Resource must not be null.");
    }
    if (!resource.exists()) {
      throw new IOException("Resource " + resource + " not found.");
    }
    this.resource = resource;
    this.content = this.normalizedContent = loadResource(resource, charset);
  }

  /**
   * Simple helper method to handle simple files.  This delegates
   * to @see #CoffeeSource(Resource) .
   *
   * @param input a File to use as input.
   * @throws java.io.IOException read file exception
   */
  public CoffeeSource(File input) throws IOException {
    this(new FileResource(input));
  }

  private String loadResource(Resource resource, Charset charset) throws IOException {
    BOMInputStream inputStream = new BOMInputStream(resource.getInputStream());
    try {
      if (inputStream.hasBOM()) {
        logger.debug("BOM found %s", inputStream.getBOMCharsetName());
        return IOUtils.toString(inputStream, inputStream.getBOMCharsetName());
      } else {
        logger.debug("Using charset " + charset.name());
        return IOUtils.toString(inputStream, charset.name());
      }
    } finally {
      inputStream.close();
    }
  }

  /**
   * Returns the absolute pathname of the LESS source.
   *
   * @return The absolute pathname of the LESS source.
   */
  public String getAbsolutePath() {
    return resource.toString();
  }

  /**
   * Returns the content of the LESS source.
   *
   * @return The content of the LESS source.
   */
  public String getContent() {
    return content;
  }

  /**
   * Returns the normalized content of the LESS source.
   * <p>
   * The normalized content represents the LESS source as a flattened source
   * where import statements have been resolved and replaced by the actual
   * content.
   * </p>
   *
   * @return The normalized content of the LESS source.
   */
  public String getNormalizedContent() {
    return normalizedContent;
  }

  /**
   * Returns the time that the LESS source was last modified.
   *
   * @return A <code>long</code> value representing the time the resource was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   */
  public long getLastModified() {
    return resource.lastModified();
  }

  public String getName() {
    return resource.getName();
  }
}
