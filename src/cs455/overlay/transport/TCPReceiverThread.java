package cs455.overlay.transport;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Judge;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.lang.*;
import java.net.SocketException;

/**
 * Created by MyGarden on 17/2/9.
 */
public class TCPReceiverThread extends Thread {

    private Socket socket;
    private DataInputStream din;
    private Node parent;

    public TCPReceiverThread(Socket socket, Node parent){
        try {
            this.socket = socket;
            din = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe){
            System.out.println("Exception: fail to create a din in TCPReceiverThread. Parent:" + parent.toString());
        }
        this.parent = parent;
    }
    public void run(){

        int dataLength;
        while (socket != null){
            try{
                dataLength = din.readInt();

                byte [] data = new byte[dataLength];
                din.readFully(data,0, dataLength);


                //RESPONSE TO DIFFERENT MESSAGE RECEIVED

                    //First, judge the type of the message, and transfer it to the certain type of unmarshalling method
                    //then get a certain type of event(message)
                Event event = Judge.decodeByte(data);

                    //call the parent to deal with this event
                parent.onEvent(event, socket);



            }catch (SocketException se){
                System.out.println(se.getMessage());
                break;
            } catch (IOException ioe){
                //Here will get an EOF when MSGNODE exit
                break;
            }

        }

    }
}
