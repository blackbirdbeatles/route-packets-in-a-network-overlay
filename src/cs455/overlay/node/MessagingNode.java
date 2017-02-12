package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;

import java.io.IOException;
import java.net.Socket;

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
    private TCPSender tcpSenderToRegistry;
    private TCPSender [] tcpSenderToMsgingNode;


    public MessagingNode(){
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSum = 0;
        receiveSum = 0;
    }



    public void onEvent(Event e){

    }

    public static void main(String [] args) {

        //get argument from command line
        String registryHost = args[0];
        int registryPort;
        try {
            registryPort = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe){
            System.out.println(nfe.getMessage());
            return;
        }

        //open the serverSocket
        MessagingNode msgNode = new MessagingNode();
        msgNode.tcpServerThread = new TCPServerThread(0);
        msgNode.tcpServerThread.start();

        //get the chosen listening port
        msgNode.listeningPort = msgNode.tcpServerThread.getListeningPort();
        System.out.println("The chosen port is " + String.valueOf(msgNode.listeningPort));


        //connect to the server
        try {
            Socket socketToRegistry = new Socket(registryHost, registryPort);
            msgNode.tcpSenderToRegistry = new TCPSender(socketToRegistry);

        //register

        // generate register message format
        Register register_msg;
        register_msg = new Register(msgNode.listeningPort, msgNode.tcpServerThread.getHostName());


        //send register message
        byte[] toSend = register_msg.getByte();

        //send the marshalled data
        msgNode.tcpSenderToRegistry.sendData(toSend);

        }
        catch (IOException ioe){
            System.out.println(ioe.getMessage());
            return;
        }






    }

}
