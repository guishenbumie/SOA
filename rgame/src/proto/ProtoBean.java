package proto;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class ProtoBean {

    private MainProto.Send.ProtoType type;
    private String protoName;
    private String handlerName;

    public ProtoBean(MainProto.Send.ProtoType type, String protoName, String handlerName) {
        this.type = type;
        this.protoName = protoName;
        this.handlerName = handlerName;
    }

    public MainProto.Send.ProtoType getType() {
        return type;
    }

    public String getProtoName() {
        return protoName;
    }

    public String getHandlerName() {
        return handlerName;
    }

}
