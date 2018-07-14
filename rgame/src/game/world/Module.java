package game.world;

import game.EModuleType;
import game.IModule;
import game.ReloadResult;

/**
 * @author sunfengmao
 * @Date 2018/6/14
 */
public class Module implements IModule {

    @Override
    public void init() {

    }

    @Override
    public void exit() {

    }

    @Override
    public ReloadResult reload() {
        return null;
    }

    @Override
    public EModuleType getETpye() {
        return EModuleType.world;
    }
}
