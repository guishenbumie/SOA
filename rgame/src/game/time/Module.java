package game.time;

import game.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class Module implements IModule {

    private ScheduledFuture<?> gameTimeTaskFuture;

    @Override
    public void init() {
        gameTimeTaskFuture = RExecutor.getInstance().scheduledAtFixedRate(
                new GameTimeTask(),
                ConfigManager.getInstance().getPropConfLong("sys", "syncGameTime.delay"),
                ConfigManager.getInstance().getPropConfLong("sys", "syncGameTime.period"),
                TimeUnit.SECONDS);
    }

    @Override
    public void exit() {
        if(null != gameTimeTaskFuture && !gameTimeTaskFuture.isCancelled()){
            gameTimeTaskFuture.cancel(true);
        }
    }

    @Override
    public ReloadResult reload() {
        return null;
    }

    @Override
    public EModuleType getETpye() {
        return EModuleType.time;
    }

}
