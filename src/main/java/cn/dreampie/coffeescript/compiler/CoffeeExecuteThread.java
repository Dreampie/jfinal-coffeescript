package cn.dreampie.coffeescript.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;

/**
 * Created by wangrenhui on 2014/7/22.
 */
public class CoffeeExecuteThread extends Observable implements Runnable {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private int restartInterval = 1000;
  private CoffeeScriptCompiler coffeeScriptCompiler;

  public CoffeeExecuteThread(CoffeeScriptCompiler coffeeScriptCompiler, int restartInterval) {
    this.coffeeScriptCompiler = coffeeScriptCompiler;
    this.restartInterval = restartInterval;
  }

  // 此方法一经调用，等待restartInterval时间之后可以通知观察者，在本例中是监听线程
  public void doBusiness() {
    logger.error("CoffeeExecuteThread is dead");
    try {
      Thread.sleep(restartInterval);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (true) {
      super.setChanged();
    }
    notifyObservers();
  }

  @Override
  public void run() {
    try {
      coffeeScriptCompiler.execute();
    } catch (CoffeeException e) {
      e.printStackTrace();
      doBusiness();
    }
  }
}
