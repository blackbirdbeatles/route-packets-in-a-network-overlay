package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by MyGarden on 17/2/11.
 */
public class Judge {

    public static Event decodeByte(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        Type type = Type.valueOf(din.readInt());
        switch (type){
            case REGISTER_REQUEST:
                return Register.decodeByte(marshalledBytes);
            case REGISTER_RESPONSE:
                return RegisterResponse.decodeByte(marshalledBytes);
            case DEREGISTER_REQUEST:
                return Deregister.decodeByte(marshalledBytes);
            case DEREGISTER_RESPONSE:
                return DeregisterResponse.decodeByte(marshalledBytes);
            case MESSAGINGNODESLIST:
                return MessagingNodesList.decodebyte(marshalledBytes);
            default:
                System.out.println("Data corrupted");
                System.exit(-1);
                return null;
        }
    }
}
