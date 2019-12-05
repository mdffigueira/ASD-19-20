package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;

public class AcceptMessage  extends ProtocolMessage {
public static  final short MSG_CODE= 202;
    AcceptMessage(int n ,){
        super(AcceptMessage.MSG_CODE);

    }
}
