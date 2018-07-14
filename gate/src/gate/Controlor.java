package gate;

/**
 * @author sunfengmao
 * @Date 2018/6/21
 */
public class Controlor implements ControlorMXBean {

    @Override
    public void stop() {
        Executor.stop();
        GateMain.isOpen = false;
    }

}
