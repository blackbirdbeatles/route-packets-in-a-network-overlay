package cs455.overlay.node;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by MyGarden on 17/2/9.
 */
public class MessagingNode implements Node {

    private int listeningPort;
    private String myIP;

    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;
    private long sendSum;
    private long receiveSum;

    private TCPServerThread tcpServerThread;


    private TCPSender tcpSenderToRegistry;
    private TCPReceiverThread tcpReceiverFromRegistry;
    private HashMap<String, Socket> currentPeer;
    private boolean peerConnection;


    private TCPReceiverThread [] tcpReceiverFromMsgingNode;


    public MessagingNode(){
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSum = 0;
        receiveSum = 0;
    }
    public String toString() {
        return "MessagingNode";
    }
    public void setMyIP(String IP){
        this.myIP = IP;
    }
    public String getMyIP(){
        return this.myIP;
    }
    //Response to the Command

    public void exitOverlay(Socket socketToRegistry){
        // send deregister message
        // generate deregister message format
        Deregister deregister_msg;
        deregister_msg = new Deregister(listeningPort, tcpServerThread.getHostName());

        byte [] toSend;
        //send deregister message
        toSend = deregister_msg.getBytes();
        //send the marshalled data
        try {
            TCPSender.sendData(toSend, socketToRegistry);
        } catch (IOException ioe){
            System.out.println("Fail for a MsgNode to send deregister event");
            System.exit(-1);
        }
    }


    private void sendHelloToPeer(Socket socketToPeer ,String host){
        Register hello = new Register(listeningPort,getMyIP());
        byte[] toSend = hello.getBytes();
        try {
            TCPSender.sendData(toSend, socketToPeer);
        } catch (IOException ioe){
            System.out.println("Fail to send Hello to " + host);
            System.exit(-1);
        }
    }

    private void helloResponseProcess(Register event, Socket socket){
        String peerHostID = event.getIP() + ":" + event.getPort();
        synchronized (currentPeer) {
            this.currentPeer.put(peerHostID, socket);
            if (currentPeer.size() == 4)
                System.out.println("All connection has done.");
        }
    }

    //Response to the Event

    public void registerResponseProcess(RegisterResponse registerResponse, Socket socket){
        boolean code = registerResponse.getCode();
        System.out.println(registerResponse.getInfo());
    }

    public void deregisterResponseProcess(DeregisterResponse deregisterResponse, Socket socket){
        boolean code = deregisterResponse.getCode();
        if (code) {
            System.out.println(deregisterResponse.getInfo());
            System.exit(0);
        }
        else
            System.out.println(deregisterResponse.getInfo());
    }

    public void messagingNodesListProcess(MessagingNodesList messagingNodesList, Socket socket){

        //initialize the currentPeerList whenever receive the MessagingNodeList
        currentPeer = new HashMap<String, Socket>();
        //Connect to all peer nodes on the Messaging Node List
        for (String host: messagingNodesList.getPeerList()){

            String[] parseHostID = host.split(":");
            String peerIP = parseHostID[0];
            String peerPort = parseHostID[1];
            Socket socketToPeer;




            try {
                //connect to the peer host
                socketToPeer = new Socket(peerIP, Integer.parseInt(peerPort));
                synchronized (currentPeer) {
                    currentPeer.put(host, socketToPeer);
                    //To see are all the connections done, "4" here is a simplized way for this assignment
                    if (currentPeer.size() == 4)
                        System.out.println("All connection has done");
                }

                //wait to prevent peer says hello to me(receive the peerlist first from the Registry), but I have not initialize my currentPeer.
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie){
                    System.out.println("Sleep is interrupted in "+ myIP + ":" + listeningPort);
                }
                sendHelloToPeer(socketToPeer, host);

                //create the receiverThread for the peer host
                TCPReceiverThread peerHost = new TCPReceiverThread(socketToPeer, this);
                peerHost.start();

            } catch (IOException ioe){
                System.out.println("Fail to connect to the peer host" + host);
            }
            //create receiver thread with peerHost
        }

    }

    public void dijkstra(){

    }

    public void linkWeightsProcess(LinkWeights event, Socket socket){
        //dijkstra;
        System.out.println("Link Weights are received and processed. Ready to send messages.");
        //print out the recieved link weight list
        //event.print();
    }

    public void onEvent(Event event, Socket socket){
        switch (event.getType()){
            case REGISTER_RESPONSE:
                registerResponseProcess((RegisterResponse)event, socket);
                break;
            case REGISTER_REQUEST:
                System.out.println("received event Hello");
                helloResponseProcess((Register)event, socket);
                break;
            case DEREGISTER_RESPONSE:
                deregisterResponseProcess((DeregisterResponse)event, socket);
                break;
            case MESSAGINGNODESLIST:
                System.out.println("received event MessagingNodesList");
                messagingNodesListProcess((MessagingNodesList)event, socket);
                break;
            case LINKWEIGHTS:
                linkWeightsProcess((LinkWeights)event, socket);
                break;
        }

    }

    public static void main(String [] args) {

        //get argument from command line
        String registryHost = args[0];
        int registryPort;
        try {
            registryPort = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe){
            System.out.println("Please input valid port");
            return;
        }

        //open the serverSocket
        MessagingNode msgNode = new MessagingNode();
        msgNode.tcpServerThread = new TCPServerThread(0, msgNode);
        msgNode.tcpServerThread.start();

        //get the chosen listening port
        msgNode.listeningPort = msgNode.tcpServerThread.getListeningPort();
        System.out.println("The chosen port is " + String.valueOf(msgNode.listeningPort));
        try {
            //connect to the server
            Socket socketToRegistry;
            socketToRegistry = new Socket(registryHost, registryPort);
            //create receiver thread with Registry
            msgNode.tcpReceiverFromRegistry = new TCPReceiverThread(socketToRegistry, msgNode);
            msgNode.tcpReceiverFromRegistry.start();
            //register

            // generate register message format
            Register register_msg;
            msgNode.setMyIP( msgNode.tcpServerThread.getHostName());
            register_msg = new Register(msgNode.listeningPort, msgNode.getMyIP());


            //send register message
            byte[] toSend = register_msg.getBytes();

            //send the marshalled data
            TCPSender.sendData(toSend,socketToRegistry);

            //wait for command from console
            Scanner scanner = new Scanner(System.in);
            String command;
            while (scanner.hasNextLine()) {
                command = scanner.nextLine();
                if (command.equals("exit-overlay")){
                    msgNode.exitOverlay(socketToRegistry);
                }




                //TODO: wait for more command of MSGNODE





            }

        } catch (IOException ioe) {
            System.out.println("Exception: Fail to connect to Registry. Exit now");
            System.exit(-1);
        }

    }
}

