package game;

/**
 * @author sunfengmao
 * @Date 2018/6/14
 */
public enum EModuleType {

    battle(1, game.battle.Module.class.getName()),
    world(2, game.world.Module.class.getName()),
    time(3, game.time.Module.class.getName()),
    ;

    private int type;
    private String clzName;

    EModuleType(int type, String clzName){
        this.type = type;
        this.clzName = clzName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getClzName() {
        return clzName;
    }

    public void setClzName(String clzName) {
        this.clzName = clzName;
    }
}
