package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/15.
 */
public class TaskComplete implements Event{

    Type type;
    String IP;
    int port;

    public TaskComplete(String IP, int port){
        type = Type.TASKCOMPLETE;
        this.IP = IP;
        this.port = port;
    }

    public byte[] getBytes(){
        try {
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
        } catch (IOException ioe){
            System.out.println("Exception: TaskComplete.getBytes()");
            System.out.println(ioe.getMessage());
            System.exit(-1);
        }
        return null;

    }
    public static Event decodebyte(byte[] marshalledBytes){
        try {
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

            return new TaskComplete(IP, port);
        } catch (IOException ioe){
            System.out.println("Fail to decode byte. (TaskComplete)");
            System.exit(-1);
        }
        return null;
    }
    public Type getType(){
        return type;
    }
    public String getIP(){
        return IP;
    }
    public int getPort(){
        return port;
    }
}
