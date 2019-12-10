package multipaxos.notifications;

import babel.notification.ProtocolNotification;
import utils.Operation;

public class OperationDone extends ProtocolNotification {
    public static final short NOTIFICATION_ID = 201;
    public static final String NOTIFICATION_NAME = "OperationDone";
    private Operation op;
    public OperationDone(Operation op){
        super(NOTIFICATION_ID,NOTIFICATION_NAME);
        this.op=op;
    }
    public Operation getOp(){
        return op;
    }
}
