package cs455.overlay.wireformats;

import java.io.*;
import java.util.Random;

/**
 * Created by MyGarden on 17/2/15.
 */
public class Payload implements Event {

    Type type;
    String destination;
    int load;

    public Payload(String destination){
        type = Type.PAYLOAD;
        Random rand = new Random();
        load = rand.nextInt();
        this.destination = destination;
    }

    public byte[] getBytes(){
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

            //write type
            dout.writeInt(type.getValue());

            //write destination string
            byte[] desBytes = destination.getBytes();
            int desLength = desBytes.length;
            dout.writeInt(desLength);
            dout.write(desBytes);

            //write load
            dout.writeInt(load);

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;
        } catch (IOException ioe){
            System.out.println("Exception: Payload.getBytes()");
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

            //readin Destination string
            int desLength = din.readInt();
            byte[] desBytes = new byte[desLength];
            din.readFully(desBytes);
            String destination = new String(desBytes);

            //readin load
            int load = din.readInt();

            baInputStream.close();
            din.close();
            Payload result = new Payload(destination);
            result.setLoad(load);
            return result;

        } catch (IOException ioe){
            System.out.println("Fail to decode byte. (Register)");
            System.exit(-1);
        }
        return null;

    };

    public Type getType(){
        return type;
    }
    public int getLoad(){
        return load;
    }
    private void setLoad(int load){
        this.load = load;
    }
    public String getDestination(){
        return  destination;
    }
}
