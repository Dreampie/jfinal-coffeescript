package cn.dreampie.coffeescript.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by wangrenhui on 2014/7/22.
 */
public class CoffeeExecuteListener implements Observer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private CoffeeExecuteThread coffeeExecuteThread;

  public CoffeeExecuteListener(CoffeeExecuteThread coffeeExecuteThread) {
    this.coffeeExecuteThread = coffeeExecuteThread;
  }

  @Override
  public void update(Observable o, Object arg) {
    coffeeExecuteThread.addObserver(this);
    new Thread(coffeeExecuteThread).start();
    logger.info("CoffeeExecuteThread is start");
  }
}
