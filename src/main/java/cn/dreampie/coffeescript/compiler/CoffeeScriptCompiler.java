package cn.dreampie.coffeescript.compiler;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Created by wangrenhui on 2014/7/11.
 */
public class CoffeeScriptCompiler extends AbstractCoffeeScript {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private CoffeeCompiler coffeeCompiler;
  /**
   * The directory for compiled CSS stylesheets.
   * <p/>
   * parameter expression="${coffeejs.outputDirectory}" default-value="${project.build.directory}"
   * required
   */
  protected File outputDirectory;

  /**
   * When <code>true</code> the coffee compiler will compress the CSS stylesheets.
   * <p/>
   * parameter expression="${coffeejs.compress}" default-value="false"
   */
  private boolean compress;

  /**
   * When <code>true</code> the plugin will watch for changes in coffee files and compile if it detects one.
   * <p/>
   * parameter expression="${coffeejs.watch}" default-value="false"
   */
  protected boolean watch = false;

  /**
   * When <code>true</code> the plugin will watch for changes in coffee files and compile if it detects one.
   * <p/>
   * parameter expression="${coffeejs.watchInterval}" default-value="1000"
   */
  private int watchInterval = 1000;

  /**
   * The character encoding the coffee compiler will use for writing the CSS stylesheets.
   * <p/>
   * parameter expression="${coffeejs.encoding}" default-value="${project.build.sourceEncoding}"
   */
  private String encoding;

  /**
   * When <code>true</code> forces the coffee compiler to always compile the coffee sources. By default coffee sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   * <p/>
   * parameter expression="${coffeejs.force}" default-value="false"
   */
  private boolean force;

  /**
   * The location of the coffee JavasSript file.
   * <p/>
   * parameter
   */
  private File coffeeJs;

  /**
   * The location of the NodeJS executable.
   * <p/>
   * parameter
   */
  private String nodeExecutable;

  /**
   * The format of the output file names.
   * <p/>
   * parameter
   */
  private String outputFileFormat;

  private static final String FILE_NAME_FORMAT_PARAMETER_REGEX = "\\{fileName\\}";

  private String[] args;

  private long lastErrorModified = 0;

  /**
   * Execute the MOJO.
   *
   * @throws cn.dreampie.coffeescript.compiler.CoffeeException if something unexpected occurs.
   */
  public void execute() throws CoffeeException {
      logger.info("sourceDirectory = " + sourceDirectory);
      logger.info("outputDirectory = " + outputDirectory);
      logger.debug("includes = " + Arrays.toString(includes));
      logger.debug("excludes = " + Arrays.toString(excludes));
      logger.debug("force = " + force);
      logger.debug("coffeeJs = " + coffeeJs);
      logger.debug("skip = " + skip);

    if (!skip) {
      if (watch) {
        logger.info("Watching " + sourceDirectory);
        if (force) {
          force = false;
          logger.info("Disabled the 'force' flag in watch mode.");
        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (watch && !Thread.currentThread().isInterrupted()) {
          executeInternal();
          try {
            Thread.sleep(watchInterval);
          } catch (InterruptedException e) {
            logger.error("interrupted");
          }
        }
      } else {
        executeInternal();
      }
    } else {
      logger.info("Skipping plugin execution per configuration");
    }
  }

  private void executeInternal() throws CoffeeException {
    String[] files = getIncludedFiles();

    if (files == null || files.length < 1) {
      logger.info("Nothing to compile - no coffee sources found");
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("included files = " + Arrays.toString(files));
      }

      Object coffeeCompiler = initCoffeeCompiler();
      compileIfChanged(files, coffeeCompiler);
    }
  }

  private void compileIfChanged(String[] files, Object coffeeCompiler) throws CoffeeException {

    for (String file : files) {
      File input = new File(sourceDirectory, file);

      buildContext.removeMessages(input);

      if (outputFileFormat != null) {
        file = outputFileFormat.replaceAll(FILE_NAME_FORMAT_PARAMETER_REGEX, file.replace(".coffee", ""));
      }

      File output = new File(outputDirectory, file.replace(".coffee", ".js"));

      if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
        throw new CoffeeException("Cannot create output directory " + output.getParentFile());
      }

      try {
        CoffeeSource coffeeSource = new CoffeeSource(input);
        long coffeeLastModified = coffeeSource.getLastModified();
        if (!output.exists() || (force || output.lastModified() < coffeeLastModified) && lastErrorModified < coffeeLastModified) {
          lastErrorModified = coffeeLastModified;
          long compilationStarted = System.currentTimeMillis();
          logger.info("Compiling coffee source: " + file);
          if (coffeeCompiler instanceof CoffeeCompiler) {
            ((CoffeeCompiler) coffeeCompiler).compile(coffeeSource, output, force);
          }
          buildContext.refresh(output);
          logger.info("Finished compilation to " + outputDirectory + " in " + (System.currentTimeMillis() - compilationStarted) + " ms");
        } else if (!watch) {
          logger.info("Bypassing coffee source: " + file + " (not modified)");
        }
      } catch (IOException e) {
//                buildContext.addMessage(input, 0, 0, "Error compiling coffee source", BuildContext.SEVERITY_ERROR, e);
        throw new CoffeeException("Error while compiling coffee source: " + file, e);
      } catch (CoffeeException e) {
        String message = e.getMessage();
        if (StringUtils.isEmpty(message)) {
          message = "Error compiling coffee source";
        }
//                buildContext.addMessage(input, 0, 0, "Error compiling coffee source", BuildContext.SEVERITY_ERROR, e);
        throw new CoffeeException("Error while compiling coffee source: " + file, e);
      }
    }

  }

  private Object initCoffeeCompiler() throws CoffeeException {

    if (coffeeCompiler == null) {
      CoffeeCompiler newCoffeeCompiler = new CoffeeCompiler();
      newCoffeeCompiler.setEncoding(encoding);
      newCoffeeCompiler.setOptionArgs(this.args);
      if (coffeeJs != null) {
        try {
          newCoffeeCompiler.setCoffeeJs(coffeeJs.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new CoffeeException("Error while loading coffee JavaScript: " + coffeeJs.getAbsolutePath(), e);
        }
      }
      coffeeCompiler = newCoffeeCompiler;
    }
    return coffeeCompiler;

  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isCompress() {
    return compress;
  }

  public void setCompress(boolean compress) {
    this.compress = compress;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }

  public int getWatchInterval() {
    return watchInterval;
  }

  public void setWatchInterval(int watchInterval) {
    this.watchInterval = watchInterval;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public File getCoffeeJs() {
    return coffeeJs;
  }

  public void setCoffeeJs(File coffeeJs) {
    this.coffeeJs = coffeeJs;
  }

  public String getNodeExecutable() {
    return nodeExecutable;
  }

  public void setNodeExecutable(String nodeExecutable) {
    this.nodeExecutable = nodeExecutable;
  }

  public String getOutputFileFormat() {
    return outputFileFormat;
  }

  public void setOutputFileFormat(String outputFileFormat) {
    this.outputFileFormat = outputFileFormat;
  }

  public void setArgs(String... args) {
    this.args = args;
  }

  public long getLastErrorModified() {
    return lastErrorModified;
  }

  public void setLastErrorModified(long lastErrorModified) {
    this.lastErrorModified = lastErrorModified;
  }
}