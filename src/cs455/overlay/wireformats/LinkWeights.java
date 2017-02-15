package cs455.overlay.wireformats;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by MyGarden on 17/2/14.
 */
public class LinkWeights implements Event {

    Type type;
    int numberOfLinks;
    ArrayList<ArrayList<Object>> weightList;

    public LinkWeights(int numberOfLinks){
        type = Type.LINKWEIGHTS;
        this.numberOfLinks = numberOfLinks;
     //   weightList = new ArrayList<>();
    }
    public byte[] getBytes(){
        try {
            byte[] marshalledBytes = null;
            ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

            //write type
            dout.writeInt(type.getValue());

            //write numberOfLinks
            dout.writeInt(numberOfLinks);

            //write numberOfLinks's String

            for (int i = 0; i < weightList.size(); i++) {
                ArrayList<Object> node1_node2_weight = weightList.get(i);
                String stringOfnode1_node2_weight = node1_node2_weight.get(0) + " " + node1_node2_weight.get(1) + " " + node1_node2_weight.get(2);
                byte[] linkInfoBytes = stringOfnode1_node2_weight.getBytes();
                int linkInfoLength = linkInfoBytes.length;
                dout.writeInt(linkInfoLength);
                dout.write(linkInfoBytes);
            }

            dout.flush();
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
            return marshalledBytes;

        } catch (IOException ioe){
            System.out.println("Exception: LinkWeights.getBytes()");
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

            //readin numberOfLink
            int numberOfLink = din.readInt();

            //readin numberOfLink's host(String)
            ArrayList<ArrayList<Object>> weightList = new ArrayList<>();

            LinkWeights linkWeights = new LinkWeights(numberOfLink);

            for (int i = 0; i < numberOfLink; i++) {
                int linkInfoLength = din.readInt();
                byte[] linkInfoBytes = new byte[linkInfoLength];
                din.readFully(linkInfoBytes);
                String onelinkInfo = new String(linkInfoBytes);
                String [] oneLineOfInfo = onelinkInfo.split(" ");
                ArrayList<Object> linkInfo = new ArrayList<>();
                linkInfo.add(oneLineOfInfo[0]);
                linkInfo.add(oneLineOfInfo[1]);
                linkInfo.add(oneLineOfInfo[2]);
                weightList.add(linkInfo);
            }

            linkWeights.copyWeightList(weightList);

            baInputStream.close();
            din.close();

            return linkWeights;

        } catch (IOException ioe){
            System.out.println("Exception: Deregister.decodeBytes()");
            System.out.println(ioe.getMessage());
            System.exit(-1);
        }
        return null;
    };
    public Type getType(){
        return type;
    }
    public int getNumberOfLinks(){
        return numberOfLinks;
    }
    public void copyWeightList(ArrayList<ArrayList<Object>> weightList){
        this.weightList = weightList;
    }
    public ArrayList<ArrayList<Object>> getWeightList(){
        return weightList;
    }
    public void print(){
        System.out.println("Type: " + type);
        System.out.println("Number of Link: " +numberOfLinks);
        for (ArrayList<Object> item: weightList){
            System.out.println(item.get(0) + " " + item.get(1) + " " + item.get(2));
        }
    }
}
