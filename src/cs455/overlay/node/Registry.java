package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by MyGarden on 17/2/8.
 */
public class Registry implements Node {

    private int port;
    private TCPServerThread tcpServerThread;
    private HashMap<String, Socket> registeredNodeList;                     //think of what kind of data structure is the best choice
    private int numberOfConnections;
    private HashMap<String, MessagingNodesList> nodeList;
    private HashMap<String, ArrayList<String>> nodeConnectsToOtherNodes;
   // private HashMap< Socket, ArrayList<ArrayList<Object>>> linkWeight;  //?????????
    private ArrayList<ArrayList<Object>> linkWeight;

    int completedNodes;


    ArrayList<Integer> numberOfMsgSent;
    ArrayList<Integer> NumberOfMsgReceived;
    ArrayList<Long> sumOfSentMsg;
    ArrayList<Long> sumOfReceivedMsg;
    ArrayList<Integer> NumberOfMsgRelayed;

    int msgSent;
    int msgReceived;
    long sum_sentMsg;
    long sum_receivedMsg;
    int msgRelayed;


    public Registry(int port) {

        this.port = port;
        registeredNodeList = new HashMap<>();
    }

    public String toString() {
        return "Registry";
    }


    //response to different kind of events

    public synchronized void trafficSummaryProcess(TrafficSummary event, Socket socket){


        numberOfMsgSent.add(event.getSendTracker());
        NumberOfMsgReceived.add(event.getReceiveTracker());
        sumOfSentMsg.add(event.getSendSum());
        sumOfReceivedMsg.add(event.getReceiveSum());
        NumberOfMsgRelayed.add(event.getRelayTracker());

        msgSent += event.getSendTracker();
        msgReceived += event.getReceiveTracker();
        sum_sentMsg += event.getSendSum();
        sum_receivedMsg += event.getReceiveSum();


        //display the message
        if (numberOfMsgSent.size() == registeredNodeList.size()){
            System.out.println("\t \t msgSent \t msgReceived \t sum_sentMsg \t sum_receivedMsg \t msgRelayed");
            for (int i = 0; i < registeredNodeList.size(); i++){
                //model: System.out.println("Node"+i+" /t "+numberOfMsgSent.get(i)+" /t "+"msgReceived"+" /t "+"sum_sentMsg"+" /t "+"sum_receivedMsg"+" /t "+"msgRelayed");
                System.out.println("Node"+(i+1)+" \t "+numberOfMsgSent.get(i)+" \t "+NumberOfMsgReceived.get(i)+" \t "+sumOfSentMsg.get(i)+" \t "+sumOfReceivedMsg.get(i)+" \t "+NumberOfMsgRelayed.get(i));
            }
            System.out.println("sum \t "+msgSent+"\t "+msgReceived+" \t "+sum_sentMsg+"\t "+sum_receivedMsg);
        }

    }

    private synchronized boolean incrementCompletedNodesAndReachAll(){
        this.completedNodes++;
        if ( completedNodes == registeredNodeList.size())
           return true;
       else
           return false;
    }
    //event: taskComplete

    private void sendPullTrafficSummary(){
        //initiate the check data
        numberOfMsgSent = new ArrayList<>();
        NumberOfMsgReceived = new ArrayList<>();
        sumOfSentMsg = new ArrayList<>();
        sumOfReceivedMsg = new ArrayList<>();
        NumberOfMsgRelayed = new ArrayList<>();

        msgSent = 0;
        msgReceived = 0;
        sum_sentMsg = 0;
        sum_receivedMsg = 0;


        ArrayList<Socket> hostSocketList = new ArrayList<>(registeredNodeList.values());

        //transfer the linkWeight class variable into the class "LinkWeights"
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();

        byte [] toSend = pullTrafficSummary.getBytes();

        for (int i = 0; i < hostSocketList.size(); i++){
            Socket socket = hostSocketList.get(i);
            try{
                TCPSender.sendData(toSend,socket);
            } catch (IOException ioe){
                System.out.println("Fail to send PullTrafficSummary message to a messagingNode. Exit now");
                System.exit(-1);
            }
        }
        System.out.println("already sent pullTrafficSummary to all nodes");

    }
    public void taskCompleteProcess(TaskComplete event,Socket socket){
        System.out.println("received task_complete from " + event.getIP() + ":" +event.getPort());

        //if all nodes completes exchanging msg, sleep 30s, and then send PULL_TRAFFIC_SUMMARY
        if (incrementCompletedNodesAndReachAll()){
            System.out.println("All the nodes have completed task, now wait for 30s");
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException ie){
                System.out.println("Interrupted in Registry");
            }
            sendPullTrafficSummary();

        }
    }

    //register and deregister
    private boolean isIdenticalIP(String IP, String realIP) {
        if (realIP.equals("127.0.0.1")) {
            if (this.tcpServerThread.getHostName().equals(IP))
                return true;
        }
        if (IP.equals(realIP))
            return true;
        return false;
    }

    //return  0    valid Register
    //return  1    fail: IP is not real
    //return  -1   fail: already registered
    private int isValidRegistration(String IP, String realIP, int port) {
        boolean isIdenticalHost = isIdenticalIP(IP, realIP);
        if (isIdenticalHost && !registeredNodeList.containsKey(IP + ":" + String.valueOf(port)))
            return 0;
        if (!isIdenticalHost)
            return 1;
        if (isIdenticalHost && registeredNodeList.containsKey(IP + ":" + String.valueOf(port)))
            return -1;

        return -2;
    }


    //return  0    valid Deregister
    //return  1    fail: IP is not real
    //return  -1   fail: could not deregister before registering
    private int isValidDeregistration(String IP, String realIP, int port) {
        boolean isIdenticalHost = isIdenticalIP(IP, realIP);
        if (isIdenticalHost && registeredNodeList.containsKey(IP + ":" + String.valueOf(port)))
            return 0;
        if (!isIdenticalHost)
            return 1;
        if (isIdenticalHost && !registeredNodeList.containsKey(IP + ":" + String.valueOf(port)))
            return -1;

        return -2;
    }

    private void registerProcess(Register register, Socket socket) {

        String IP = register.getIP();
        String realIP = socket.getInetAddress().getHostAddress();
        int port = register.getPort();
        String hostID = IP + ":" +port;
        System.out.println("Received register event from " + hostID);

        //to prevent multi-threads access the registeredNodeList at the same time
        synchronized (this.registeredNodeList) {
            int isValid = isValidRegistration(IP, realIP, port);

            //if register succeed, send success response
            if (isValid == 0) {
                //add the current node to registeredNodeList
                registeredNodeList.put(IP + ":" + String.valueOf(port), socket);

                //send response packet to the messaging node
                String info = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodeList.size() + ")";
                RegisterResponse registerResponse = new RegisterResponse(true, info);
                try {
                    TCPSender.sendData(registerResponse.getBytes(), socket);
                } catch (IOException ioe) {
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");

                    //TODO: SEND A "INTERUPT" EVENT TO REGISTRY AND DELETE THE MEMBERSHIP FOR THIS NODE

                    System.exit(-1);
                }


            }

            //if registering fail, send failure response
            else {
                String info;
                if (isValid == 1)
                    info = "Fail to Register: IP is not real";
                else if (isValid == -1)
                    info = "Fail to Register: IP already registered";
                else
                    info = "The value of isValid is -2";
                RegisterResponse registerResponse = new RegisterResponse(false, info);
                try {
                    TCPSender.sendData(registerResponse.getBytes(), socket);
                } catch (IOException ioe) {
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");
                    System.exit(-1);
                }
            }
        }

    }



    private void deregisterProcess(Deregister deregister, Socket socket) {

        String IP = deregister.getIP();
        int port = deregister.getPort();
        String realIP = socket.getInetAddress().getHostAddress();
        System.out.println("Received deregister event from "+ IP + ":" + port);

        //to prevent multi-threads access the registeredNodeList at the same time
        synchronized (this.registeredNodeList) {
            //The valid standard for deregister is the same as register
            int isValid = isValidDeregistration(IP, realIP, port);

            //if deregister succeed, send success response
            if (isValid == 0) {
                //delete the current node from registeredNodeList
                registeredNodeList.remove(IP + ":" + String.valueOf(port));

                //send success response packet to the messaging node
                String info = "Deregistration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodeList.size() + ")";
                DeregisterResponse deregisterResponse = new DeregisterResponse(true, info);
                try {
                    TCPSender.sendData(deregisterResponse.getBytes(), socket);
                } catch (IOException ioe) {
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");

                    //TODO: SEND A "INTERUPT" EVENT TO REGISTRY AND DELETE THE MEMBERSHIP FOR THIS NODE

                    System.exit(-1);
                }


            }

            //if deregistering fail, send failure response
            else {
                String info;
                if (isValid == 1)
                    info = "Fail to Deregister: IP is not real";
                else if (isValid == -1)
                    info = "Fail to Deregister: could not deregister before registering";
                else
                    info = "The value of isValid is -2";
                DeregisterResponse deregisterResponse = new DeregisterResponse(false, info);
                try {
                    TCPSender.sendData(deregisterResponse.getBytes(), socket);
                } catch (IOException ioe) {
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");
                    System.exit(-1);
                }
            }
        }

    }


    //*****************Methods to response to command from console




    //SETUP-OVERLAY: send MessagingNodeList to the corresponding host


    private void buildNodeList(){

        //traverse through nodeConnectsToOtherNodes, create a nodeList <String, MessagingNodeList>

        ArrayList<String> hostList = new ArrayList<>(this.nodeConnectsToOtherNodes.keySet());
        for (int i = 0; i < hostList.size(); i++){
            String host = hostList.get(i);
            ArrayList<String> peerList = this.nodeConnectsToOtherNodes.get(host);

            //create the event for one certain host according to peerList: MessagingNodesList
            MessagingNodesList messagingNodesList = new MessagingNodesList(peerList.size());
            messagingNodesList.copyPeerList(peerList);
            nodeList.put(host, messagingNodesList);
        }
    }

    private void sendMessagingNodeList(){
        //MessagingNodesList messagingNodesList = new MessagingNodesList();

        //after buildNodeList(), we get the this.nodeList: hashmap <host, MessagingNodeList>
        buildNodeList();
        ArrayList<String> hostList = new ArrayList<>(this.nodeList.keySet());
        for (int i = 0; i < hostList.size(); i++){
            String host = hostList.get(i);
            Socket socket = this.registeredNodeList.get(host);
            MessagingNodesList messagingNodesList = this.nodeList.get(host);
            byte [] toSend = messagingNodesList.getBytes();
            try {
                TCPSender.sendData(toSend,socket);
            } catch (IOException e) {
                // Alreay after setup-overlay, should not delete the current node, just exite
                System.out.println("Fail to send messagingNodeList to" + host + ". Exit now ");
                System.exit(-1);
            }
            System.out.println("Already sent MessagingNodeList to " + host);
        }
    }

    private void setupLink(){

        //ALGORITHM:
        //link setup first relate the adjacent nodes, each nodes got 2 links. Then relate every two nodes, each also got 2 links.

        //What to do:
        //put link info into the linkWeight arraylist (ready to send the weight event to all nods)
        // classify link into this.connectToOtherNodes for the event of node_list


        Random rand = new Random();
        ArrayList<String> hostList = new ArrayList<>(this.registeredNodeList.keySet());

        //initiate node nodeConnectsToOtherNodes Hashmap <String, ArrayList<String>>
        nodeConnectsToOtherNodes = new HashMap<String, ArrayList<String>>();
        linkWeight = new ArrayList<>();
        nodeList = new HashMap<>();
        for (int i = 0; i < hostList.size(); i++)
            nodeConnectsToOtherNodes.put(hostList.get(i),new ArrayList<>());


        //relate adjacent nodes
        for (int i = 0; i < hostList.size(); i++){

            ArrayList<Object> linkInfo = new ArrayList<>();
            String connectFromMsgNode = hostList.get(i%hostList.size());
            String connectToMsgNode = hostList.get((i+1)%hostList.size());
            Integer weight = new Integer(rand.nextInt(10)+1);

            linkInfo.add(connectFromMsgNode);
            linkInfo.add(connectToMsgNode);
            linkInfo.add(weight);

            this.nodeConnectsToOtherNodes.get(connectFromMsgNode).add(connectToMsgNode);
            linkWeight.add(linkInfo);
        }

        //relate every two consecutive nodes
        for (int i = 0; i < hostList.size(); i++){

            ArrayList<Object> linkInfo = new ArrayList<>();
            String connectFromMsgNode = hostList.get(i%hostList.size());
            String connectToMsgNode = hostList.get((i+2)%hostList.size());
            Integer weight = new Integer(rand.nextInt(10)+1);

            linkInfo.add(connectFromMsgNode);
            linkInfo.add(connectToMsgNode);
            linkInfo.add(weight);

            this.nodeConnectsToOtherNodes.get(connectFromMsgNode).add(connectToMsgNode);
            linkWeight.add(linkInfo);
        }
    }

    public void setupOverlay(){

        //get the linkWeight ArrayList<ArrayList<Object>> , which is the topology of the host net
        setupLink();
        //notify each of the node who they should connect to;
        sendMessagingNodeList();
    }

    public void listMessagingNodes() {
        for (String IPandPort : this.registeredNodeList.keySet()) {
            System.out.println(IPandPort);
        }
    }

    public void listWeight(){
        for (int i = 0; i < linkWeight.size(); i++){
            System.out.println(linkWeight.get(i).get(0) + "  " + linkWeight.get(i).get(1) + "  " + linkWeight.get(i).get(2));
        }
    }
    public void sendOverlayLinkWeights(){
        ArrayList<Socket> hostSocketList = new ArrayList<>(registeredNodeList.values());

        //transfer the linkWeight class variable into the class "LinkWeights"
        LinkWeights linkWeights = new LinkWeights(this.linkWeight.size());
        linkWeights.copyWeightList(this.linkWeight);
        byte [] toSend = linkWeights.getBytes();

        for (int i = 0; i < hostSocketList.size(); i++){
            Socket socket = hostSocketList.get(i);
            try{
                TCPSender.sendData(toSend,socket);
            } catch (IOException ioe){
                System.out.println("Fail to send linkWeight message to a messagingNode. Exit now");
                System.exit(-1);
            }
        }

    }

    public void onEvent(Event event, Socket socket){
        switch (event.getType()){
            case REGISTER_REQUEST:
                registerProcess((Register)event, socket);
                break;
            case DEREGISTER_REQUEST:
                deregisterProcess((Deregister)event, socket);
                break;
            case TASKCOMPLETE:
                taskCompleteProcess((TaskComplete)event,socket);
                break;
            case TRAFFICSUMMARY:
                trafficSummaryProcess((TrafficSummary)event,socket);
                break;
        }
    }

    private void sendInitiateToAllHost(int rounds){

        ArrayList<Socket> hostSocketList = new ArrayList<>(registeredNodeList.values());

        //transfer the linkWeight class variable into the class "LinkWeights"
        TaskInitiate taskInitiate = new TaskInitiate(rounds);
        byte [] toSend = taskInitiate.getBytes();

        for (int i = 0; i < hostSocketList.size(); i++){
            Socket socket = hostSocketList.get(i);
            try{
                TCPSender.sendData(toSend,socket);
            } catch (IOException ioe){
                System.out.println("Fail to send task_initiate message to a messagingNode. Exit now");
                System.exit(-1);
            }
        }
    }
    public void startProcess(int rounds){
        sendInitiateToAllHost(rounds);

        // clear up the current start completed exchanging packets nodes count
        this.completedNodes = 0;

        //TODO: keep doing about start
    }


    public static void main(String [] args){

        //get port from command
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe){
            System.out.println("Pleas input valid port");
            return;
        }

        //create registry object and initialize it
        Registry registry = new Registry(port);

        //open the server thread on the port
        registry.tcpServerThread = new TCPServerThread(registry.port, registry);
        registry.tcpServerThread.start();

        //wait for command from console
        Scanner scanner = new Scanner(System.in);
        String command;
        while (scanner.hasNextLine()) {

            command = scanner.nextLine();

            if (command.equals("list-messaging nodes")){
                registry.listMessagingNodes();
                continue;
            }
            if (command.equals("list-weights")){
                registry.listWeight();
                continue;

            }
            if (command.equals("send-overlay-link-weights")){
                registry.sendOverlayLinkWeights();
                continue;
            }
            if (command.startsWith("setup-overlay ")) {
                String subCommand = command.substring(14);
                try{
                    registry.numberOfConnections = Integer.parseInt(subCommand);
                } catch (NumberFormatException nfe){
                    System.out.println("Please enter right format of command");
                    continue;
                }

                //TODO: Here I do not quite know what if the number of current nodes is less than 10, since that will break my setupOverlay procedure.
                if (registry.registeredNodeList.size() >= registry.numberOfConnections)
                    registry.setupOverlay();
                else{
                    System.out.println("Error: the number of messaging nodes is less than the connection limit that is specified");
                    continue;
                }
                continue;
            }
            if (command.startsWith("start")){
                String subCommand = command.substring(6);
                int rounds;
                try{
                    rounds = Integer.parseInt(subCommand);
                } catch (NumberFormatException nfe){
                    System.out.println("Please enter right format of command");
                    continue;
                }
                registry.startProcess(rounds);
                continue;
            }
            System.out.println("Command wrong in Registry");


        }
    }
}
