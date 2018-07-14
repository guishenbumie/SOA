package game;

/**
 * @author sunfengmao
 * @Date 2018/6/15
 */
public enum EReloadType {

    RELOAD_PROPERTIES(1, "热加载properties配置"),
    RELOAD_XML(2, "热加载xml配置"),
    RELOAD_MODULE(3, "热加载模块"),
    ;

    private int type;
    private String name;

    EReloadType(int type, String name){
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
