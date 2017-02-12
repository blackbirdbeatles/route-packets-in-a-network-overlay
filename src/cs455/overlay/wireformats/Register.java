package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/11.
 */
public class Register implements Event{

    MessageType type;
    String IP;
    int port;

    public Register(int port, String IP){
        type = MessageType.REGISTER_REQUEST;
        this.IP = IP;
        this.port = port;
    }

    public byte[] getBytes() throws IOException {
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

    public static Register decodeByte(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //readin type, but actually already known
        din.readInt();

        //readin IP string
        int IPLength = din.readInt();
        byte[] IPBytes = new byte[IPLength];
        din.readFully(IPBytes);
        String IP = new String(IPBytes);

        //readin port
        int port = din.readInt();

        baInputStream.close();
        din.close();

        return new Register(port,IP);


    }


}
