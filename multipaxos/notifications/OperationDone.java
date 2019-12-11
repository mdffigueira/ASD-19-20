package multipaxos.notifications;

import babel.notification.ProtocolNotification;
import utils.Operation;

public class OperationDone extends ProtocolNotification {
    public static final short NOTIFICATION_ID = 201;
    public static final String NOTIFICATION_NAME = "OperationDone";
    private Operation op;
    private int instanceNumber, sequenceNumber;

    public OperationDone(Operation op, int instanceNumber, int sequenceNumber) {
        super(NOTIFICATION_ID, NOTIFICATION_NAME);
        this.op = op;
        this.instanceNumber = instanceNumber;
        this.sequenceNumber = sequenceNumber;
    }

    public Operation getOp() {
        return op;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
