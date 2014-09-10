package cn.dreampie.coffeescript;

import cn.dreampie.coffeescript.compiler.*;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.ThreadBuildContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangrenhui on 2014/7/11.
 */
public class CoffeeScriptPlugin implements IPlugin {
  private Logger logger = LoggerFactory.getLogger(getClass());
  //restart thread  timeout
  private int restartInterval = 1000;
  private CoffeeScriptCompiler coffeeScriptCompiler;

  public CoffeeScriptPlugin() {
    setCoffeeScriptCompiler("/coffeescript/", "/javascript/", false, "--bare", true);

  }

  public CoffeeScriptPlugin(String in, String out) {
    setCoffeeScriptCompiler(in, out, false, "--bare", true);
  }


  public CoffeeScriptPlugin(CoffeeScriptCompiler coffeeScriptCompiler) {
    this.coffeeScriptCompiler = coffeeScriptCompiler;
  }

  public CoffeeScriptPlugin(int restartInterval, CoffeeScriptCompiler coffeeScriptCompiler) {
    this.restartInterval = restartInterval;
    this.coffeeScriptCompiler = coffeeScriptCompiler;
  }


  private void setCoffeeScriptCompiler(String in, String out, boolean compress, String args, boolean watch) {
    coffeeScriptCompiler = new CoffeeScriptCompiler();
    coffeeScriptCompiler.setBuildContext(ThreadBuildContext.getContext());
    coffeeScriptCompiler.setSourceDirectory(new File(PathKit.getWebRootPath() + in));
    coffeeScriptCompiler.setOutputDirectory(new File(PathKit.getWebRootPath() + out));
//        coffeeScriptCompiler.setForce(true);
    coffeeScriptCompiler.setCompress(compress);
    coffeeScriptCompiler.setArgs(args);
    coffeeScriptCompiler.setWatch(watch);
  }

  @Override
  public boolean start() {
    CoffeeExecuteThread run = new CoffeeExecuteThread(coffeeScriptCompiler, restartInterval);
    CoffeeExecuteListener listen = new CoffeeExecuteListener(run);
    run.addObserver(listen);
    new Thread(run).start();
    return true;
  }

  @Override
  public boolean stop() {
    return false;
  }

  public static void main(String[] args) throws IOException, CoffeeException {
    CoffeeCompiler coffeeCompiler = new CoffeeCompiler();
    String js = coffeeCompiler.compile("alert '测试'");
//        System.out.println(js);

    coffeeCompiler = new CoffeeCompiler();
    js = coffeeCompiler.compile(new File(PathKit.getWebRootPath() + "/src/main/webapp/javascript/app/main.coffee"));
//        System.out.println(js);

    CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler();
    coffeeScriptCompiler.setBuildContext(ThreadBuildContext.getContext());
    coffeeScriptCompiler.setSourceDirectory(new File(PathKit.getWebRootPath() + "/src/main/webapp/javascript/"));
//        coffeeScriptCompiler.setOutputDirectory(new File(PathKit.getRootClassPath() + "/javascript/"));
    coffeeScriptCompiler.setForce(true);
    coffeeScriptCompiler.setArgs("--bare");
    coffeeScriptCompiler.setWatch(true);
    coffeeScriptCompiler.execute();
  }

}
