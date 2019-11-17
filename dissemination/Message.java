package dissemination;

import java.util.UUID;

public class Message {
    UUID mid ;
    byte[] message,topic;
    int typeM;


    public  Message(byte[] message,int typeM,byte[] topic){
        this.mid = UUID.randomUUID();
        this.message = message;
        this.typeM = typeM;
        this.topic = topic;
    }
    public UUID getMid(){
        return mid;

    }
    public byte[] getMessage(){
        return message;

    }
    public byte[] getTopic(){
        return topic;
    }
    public int getTypeM(){
        return typeM;
    }

}
