package utils;

public class Operation {
    public static final String ADD_REPLICA = "Add Replica";
    public static final String REMOVE_REPLICA = "Remove Replica";
    public static final String PUBLISH = "Publish";

    private String type;
    private Message msg;

    public Operation(String type, Message msg) {
        this.type = type;
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }

    public String getType() {
        return type;
    }
}
