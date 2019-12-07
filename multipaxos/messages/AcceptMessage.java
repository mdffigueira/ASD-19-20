package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Operation;

public class AcceptMessage  extends ProtocolMessage {
public static  final short MSG_CODE= 202;
    AcceptMessage(int n , Operation op,int seqnumb){
        super(AcceptMessage.MSG_CODE);

    }
}
