package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by MyGarden on 17/2/11.
 */
public class Register {

    MessageType type;
    String IP;
    int port;

    public Register(int port, String IP){
        type = MessageType.REGISTER_REQUEST;
        this.IP = IP;
        this.port = port;
    }

    public byte[] getByte() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //write type
        dout.writeInt(type.getValue());

        //write IP string
        byte[] IPBytes = IP.getBytes();
        int IPLength = IPBytes.length;
        dout.writeInt(IPLength);
        dout.write(IPBytes);

        //write port
        dout.writeInt(port);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    public static Event decodeByte() throws IOException{
        Event e;
        return e;
    }


}
