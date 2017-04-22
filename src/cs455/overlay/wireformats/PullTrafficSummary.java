package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/15.
 */
public class PullTrafficSummary implements Event {
    private Type type;

    public PullTrafficSummary(){
        type = Type.PULLTRAFFICSUMMARY;
    }

    public byte[] getBytes(){
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

            //write type
            dout.writeInt(type.getValue());

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;
        } catch (IOException ioe){
            System.out.println("Exception: PullTrafficSummary.getBytes()");
            System.out.println(ioe.getMessage());
            System.exit(-1);
        }
        return null;
    }
    public static Event decodebyte(byte[] marshalledBytes) {
        try {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

            //readin type, but actually already known
            din.readInt();

            baInputStream.close();
            din.close();

            return new PullTrafficSummary();
        } catch (IOException ioe){
            System.out.println("Fail to decode byte. (PullTrafficSummary)");
            System.exit(-1);
        }
        return null;

    }
    public Type getType(){
        return type;
    }
}
