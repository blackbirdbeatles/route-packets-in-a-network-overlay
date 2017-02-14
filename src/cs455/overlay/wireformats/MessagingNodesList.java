package cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by MyGarden on 17/2/13.
 */
public class MessagingNodesList implements Event {

    private Type type;
    private int numberOfPeer;
    private ArrayList<String> peerList;

    public MessagingNodesList(int numberOfPeer){
        this.type = Type.MESSAGINGNODESLIST;
        this.numberOfPeer = numberOfPeer;
    }

    public byte[] getBytes(){
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

            //write type
            dout.writeInt(type.getValue());

            //write numberOfPear
            dout.writeInt(numberOfPeer);

            //write numberOfPeer's string

            for (int i = 0; i < peerList.size(); i++) {
                byte[] hostBytes = peerList.get(1).getBytes();
                int hostLength = hostBytes.length;
                dout.writeInt(hostLength);
                dout.write(hostBytes);
            }

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;

        } catch (IOException ioe){
            System.out.println("Exception: Register.getBytes()");
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

            //readin numberOfPeer
            int numberOfPeer = din.readInt();

            //readin numberOfPeer's host(string)
            ArrayList<String> peerList = new ArrayList<>();
            for (int i = 0; i < numberOfPeer; i++) {
                int hostLength = din.readInt();
                byte[] hostBytes = new byte[hostLength];
                din.readFully(hostBytes);
                String host = new String(hostBytes);
                peerList.add(host);
            }

            baInputStream.close();
            din.close();
            MessagingNodesList result = new MessagingNodesList(numberOfPeer);
            result.copyPeerList(peerList);

            return result;

        } catch (IOException ioe){
            System.out.println("Exception: Deregister.decodeBytes()");
            System.out.println(ioe.getMessage());
            System.exit(-1);
        }
        return null;
    }
    public Type getType(){
        return type;
    }
    public int getNumberOfPeer(){
        return numberOfPeer;
    }
    public void copyPeerList(ArrayList<String> peerList){
        this.peerList = peerList;
    }
}
