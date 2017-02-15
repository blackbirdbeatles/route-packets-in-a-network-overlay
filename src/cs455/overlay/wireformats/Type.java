package cs455.overlay.wireformats;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MyGarden on 17/2/11.
 */
public enum Type {

    REGISTER_REQUEST(1),
    REGISTER_RESPONSE(2),

    DEREGISTER_REQUEST(3),
    DEREGISTER_RESPONSE(4),

    MESSAGINGNODESLIST(5),
    LINKWEIGHTS(6),
    TASKINITIATE(7),
    PAYLOAD(8),
    TASKCOMPLETE(9),
    PULLTRAFFICSUMMARY(10),
    TRAFFICSUMMARY(11);


    private int value;
    private static Map<Integer, Type> map = new HashMap<Integer, Type>();

    private Type(int value){
        this.value = value;
    }

    static {
        for (Type messageType : Type.values()){
            map.put(messageType.value, messageType);
        }
    }

    public static Type valueOf(int messageType){
        return map.get(messageType);
    }

    public int getValue(){
        return value;
    }
}