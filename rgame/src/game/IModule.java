package game;

/**
 * @author sunfengmao
 * @Date 2018/6/7
 */
public interface IModule {

    public void init();
    public void exit();
    public ReloadResult reload();
    public EModuleType getETpye();

}
