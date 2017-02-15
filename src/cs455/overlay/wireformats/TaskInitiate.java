package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/15.
 */
public class TaskInitiate implements Event {

    Type type;
    int rounds;

    public TaskInitiate(int rounds){
        type = Type.TASKINITIATE;
        this.rounds = rounds;
    }

    public byte[] getBytes(){
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

            //write type
            dout.writeInt(type.getValue());

            //write rounds
            dout.writeInt(rounds);

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;
        } catch (IOException ioe){
            System.out.println("Exception: TaskInitiate.getBytes()");
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

            //readin rounds
            int rounds = din.readInt();

            baInputStream.close();
            din.close();

            return new TaskInitiate(rounds);
        } catch (IOException ioe){
            System.out.println("Fail to decode byte. (TaskInitiate)");
            System.exit(-1);
        }
        return null;
    }
    public Type getType(){
        return type;
    }
    public int getRounds(){
        return rounds;
    }
}
