package web.message;

public enum MessageType {
    SPAWN_CAR(1),
    ;

    private int code;

    MessageType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}
