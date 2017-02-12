package cs455.overlay.wireformats;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MyGarden on 17/2/11.
 */
public enum MessageType {
    REGISTER_REQUEST(1);

    private int value;
    private static Map<Integer, MessageType> map = new HashMap<Integer, MessageType>();

    private MessageType(int value){
        this.value = value;
    }

    static {
        for (MessageType messageType : MessageType.values()){
            map.put(messageType.value, messageType);
        }
    }

    public static MessageType valueOf(int messageType){
        return map.get(messageType);
    }

    public int getValue(){
        return value;
    }
}

