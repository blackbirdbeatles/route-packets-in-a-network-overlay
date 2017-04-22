package cs455.overlay.node;

import cs455.overlay.dijkstra.ShortestPath;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by MyGarden on 17/2/9.
 */
public class MessagingNode implements Node {

    private int listeningPort;
    private String myIP;
    private String myHostID;

    private int sendTracker;
    private int receiveTracker;
    private int relayTracker;
    private long sendSum;
    private long receiveSum;

    private TCPServerThread tcpServerThread;
    Socket socketToRegistry;


    private TCPSender tcpSenderToRegistry;
    private TCPReceiverThread tcpReceiverFromRegistry;
    private HashMap<String, Socket> currentPeer;
    private boolean peerConnection;

    private HashMap<String, String> routingPlan;
    ShortestPath dijkstra;
    private ArrayList<String> allHostList;


    private TCPReceiverThread [] tcpReceiverFromMsgingNode;


    public MessagingNode(){
    //    this.routingReady = false;

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

    public void printShortestPath(){
        if (dijkstra != null) {
            dijkstra.printPath();
        }
        else
            System.out.println("Lack link weights");
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


    //pull traffic Summary
    private void sendTrafficSummary(){
        TrafficSummary trafficSummary = new TrafficSummary(getMyIP(), listeningPort,sendTracker,sendSum,receiveTracker,receiveSum,relayTracker);
        byte [] toSend = trafficSummary.getBytes();
        try {
            TCPSender.sendData(toSend, socketToRegistry);
        } catch (IOException ioe){
            System.out.println("Fail to send summary to registry");
            System.exit(-1);
        }
    }

    public void pullTrafficSummaryProcess(PullTrafficSummary event, Socket socket){
        System.out.println("received pull traffic summary from registry");
        sendTrafficSummary();

    }

    private synchronized void incrementReceiveTracker(){
        this.receiveTracker++;
    }
    private synchronized void incrementReceiveSum(int load){
        this.receiveSum += load;
    }
    private synchronized void incrementRelayTracker(){
        this.relayTracker++;
    }

    public void payloadProcess(Payload event, Socket socket){
        //System.out.println("received Payload");
        if (event.getDestination().equals(myHostID)){
            incrementReceiveTracker();
            incrementReceiveSum(event.getLoad());
        }
        else{
            routingMessage(event.getDestination(), event);
            incrementRelayTracker();
        }

    }

    private String getPickedPeer(){
        Random rand = new Random();
        Integer myNumber = allHostList.indexOf(myHostID);
        Integer hostNumber = new Integer(rand.nextInt(allHostList.size()));
        while (hostNumber == myNumber)
            hostNumber = new Integer(rand.nextInt(allHostList.size()));
        return allHostList.get(hostNumber);
    }

    private void routingMessage(String destination, Payload payload){
        //send payload event to the destination
        byte [] toSend = payload.getBytes();
        try {
            TCPSender.sendData(toSend, currentPeer.get(routingPlan.get(destination)));
        } catch (IOException ioe){
            System.out.println("Fail to send payload message to " + destination);
            System.exit(-1);
        }
    }

    private void incrementSendTracker(){
        this.sendTracker++;
    }
    private void incrementSendSum(int load){
        this.sendSum += load;
    }

    public void taskInitiateProcess(TaskInitiate event, Socket socket){

        System.out.println("Received TaskInitiate");
        //initiate
        sendTracker = 0;
        receiveTracker = 0;
        relayTracker = 0;
        sendSum = 0;
        receiveSum = 0;

        //wait for some time
        try {
            TimeUnit.MILLISECONDS.sleep(1500);
        } catch (InterruptedException ie){
            System.out.println("MsgNode is interrupted when initiate task");
        }


        //exchange information
        for (int i = 0; i < event.getRounds(); i++) {
            //pick a peer host to send 5 messages
            String pickedPeer = getPickedPeer();
            //System.out.println("pick destination: " +pickedPeer );

            //send message to peer host
            for (int j = 0; j < 5; j++){
                Payload payload = new Payload(pickedPeer);
                routingMessage(pickedPeer, payload);
                incrementSendTracker();
                incrementSendSum(payload.getLoad());
               // System.out.println("Done messaging " + j + " out of 5");
            }
            if ((i % 1000) == 0)
                System.out.println(i);
        }
        System.out.println("Task Completed");

        //send complete message to Registry
        TaskComplete taskComplete = new TaskComplete(myIP, listeningPort);
        byte[] toSend = taskComplete.getBytes();
        try {
            TCPSender.sendData(toSend, socketToRegistry);
        } catch (IOException ioe){
            System.out.println("Fail to send completion task to registry.");
            System.exit(-1);
        }

    }


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
        System.out.println("Received MessagingNodesList");

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


    public void linkWeightsProcess(LinkWeights event, Socket socket){
        dijkstra = new ShortestPath(event.getWeightList(), this.myHostID);
        this.routingPlan = dijkstra.getRountingPlan();
        dijkstra.printRoutingPlan();
        this.allHostList = dijkstra.getAllHostArray();
       // this.routingReady = true;
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
                messagingNodesListProcess((MessagingNodesList)event, socket);
                break;
            case LINKWEIGHTS:
                linkWeightsProcess((LinkWeights)event, socket);
                break;
            case TASKINITIATE:
                taskInitiateProcess((TaskInitiate)event, socket);
                break;
            case PAYLOAD:
                payloadProcess((Payload)event,socket);
                break;
            case PULLTRAFFICSUMMARY:
                pullTrafficSummaryProcess((PullTrafficSummary)event,socket);
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
            msgNode.socketToRegistry = new Socket(registryHost, registryPort);
            //create receiver thread with Registry
            msgNode.tcpReceiverFromRegistry = new TCPReceiverThread(msgNode.socketToRegistry, msgNode);
            msgNode.tcpReceiverFromRegistry.start();
            //register

            // generate register message format
            Register register_msg;
            msgNode.setMyIP( msgNode.tcpServerThread.getHostName());
            msgNode.myHostID = msgNode.getMyIP() + ":" + msgNode.listeningPort;
            register_msg = new Register(msgNode.listeningPort, msgNode.getMyIP());


            //send register message
            byte[] toSend = register_msg.getBytes();

            //send the marshalled data
            TCPSender.sendData(toSend,msgNode.socketToRegistry);

            //wait for command from console
            Scanner scanner = new Scanner(System.in);
            String command;
            while (scanner.hasNextLine()) {
                command = scanner.nextLine();
                if (command.equals("exit-overlay")){
                    msgNode.exitOverlay(msgNode.socketToRegistry);
                    continue;
                }
                if (command.equals("print-shortest-path")){
                    msgNode.printShortestPath();
                    continue;
                }
                System.out.println("bad match command");



                //TODO: wait for more command of MSGNODE



            }

        } catch (IOException ioe) {
            System.out.println("Exception: Fail to connect to Registry. Exit now");
            System.exit(-1);
        }

    }
}

