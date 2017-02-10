package cs455.overlay.node;

import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;

/**
 * Created by MyGarden on 17/2/9.
 */
public class MessagingNode implements Node {

    private int listeningPort;
    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;
    private long sendSum;
    private long receiveSum;
    private TCPServerThread tcpServerThread;

    public MessagingNode(int port){
        listeningPort = port;
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSum = 0;
        receiveSum = 0;
        tcpServerThread = new TCPServerThread(port);
    }



    public void onEvent(Event e){

    }

    public static void main(String [] args){

        int port = Integer.parseInt(args[0]);
        MessagingNode msgNode = new MessagingNode(port);

        try {



        }catch(Exception e){
            e.getMessage();
        }

    }

}
