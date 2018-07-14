package game;

/**
 * @author sunfengmao
 * @Date 2018/6/7
 */
public class ReloadResult {

    private boolean success;
    private StringBuilder msg;

    public ReloadResult(boolean success){
        this.success = success;
        this.msg = new StringBuilder("");
    }

    public ReloadResult(boolean success, String msg){
        this.success = success;
        this.msg = new StringBuilder(msg);
    }

    public void fail(){
        success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg.toString();
    }

    public void appendMsg(String str){
        msg.append(str);
    }

}
