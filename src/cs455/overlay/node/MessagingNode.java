package cs455.overlay.node;

import cs455.overlay.wireformats.Event;

/**
 * Created by MyGarden on 17/2/9.
 */
public class MessagingNode implements Node {

    private int listeningPort;
    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;

    public MessagingNode(int port) throws Exception {
        listeningPort = port;

    }



    public void onEvent(Event e){

    }

    public static void main(String [] args){
        int port = Integer.parseInt(args[0]);
        MessagingNode msgNode(port);

        try {



        }catch(Exception e){
            e.getMessage();
        }

    }

}
