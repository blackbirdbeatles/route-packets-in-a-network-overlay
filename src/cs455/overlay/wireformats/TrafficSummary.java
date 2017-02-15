package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/15.
 */
public class TrafficSummary implements Event {

    private Type type;
    private String IP;
    private int port;

    private int sendTracker;
    private long sendSum;
    private int receiveTracker;
    private long receiveSum;
    private int relayTracker;

    public TrafficSummary(String IP, int port,int sendTracker,long sendSum,int receiveTracker,long receiveSum,int relayTracker){
        this.type = Type.TRAFFICSUMMARY;
        this.IP = IP;
        this.port = port;
        this.sendTracker = sendTracker;
        this.sendSum =sendSum;
        this.receiveTracker =receiveTracker;
        this.receiveSum =receiveSum;
        this.relayTracker = relayTracker;
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

            dout.writeInt(sendTracker);
            dout.writeLong(sendSum);
            dout.writeInt(receiveTracker);
            dout.writeLong(receiveSum);
            dout.writeInt(relayTracker);

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;
        } catch (IOException ioe){
            System.out.println("Exception: TrafficSummary.getBytes()");
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

            int sendTracker = din.readInt();
            long sendSum = din.readLong();
            int receiveTracker = din.readInt();
            long receiveSum = din.readLong();
            int relayTracker = din.readInt();

            baInputStream.close();
            din.close();

            return new TrafficSummary(IP,port,sendTracker,sendSum,receiveTracker,receiveSum,relayTracker);
        } catch (IOException ioe){
            System.out.println("Fail to decode byte. (Register)");
            System.exit(-1);
        }
        return null;


    }
    public Type getType(){
        return type;
    }
    public String getIP(){
        return  IP;
    }
    public int getPort(){
        return  port;
    }
    public int getSendTracker(){
        return sendTracker;
    }
    public long getSendSum(){
        return sendSum;
    }
    public int getReceiveTracker(){
        return receiveTracker;
    }
    public long getReceiveSum(){
        return receiveSum;
    }
    public int getRelayTracker(){
        return relayTracker;
    }
}
