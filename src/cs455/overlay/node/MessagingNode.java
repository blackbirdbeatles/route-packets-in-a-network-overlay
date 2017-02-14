package cs455.overlay.node;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

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
    private TCPReceiverThread tcpReceiverFromRegistry;


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

    }

    public void onEvent(Event event, Socket socket){
        switch (event.getType()){
            case REGISTER_RESPONSE:
                registerResponseProcess((RegisterResponse)event, socket);
                break;
            case DEREGISTER_RESPONSE:
                deregisterResponseProcess((DeregisterResponse)event, socket);
                break;
            case MESSAGINGNODESLIST:
                messagingNodesListProcess((MessagingNodesList)event, socket);


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
            register_msg = new Register(msgNode.listeningPort, msgNode.tcpServerThread.getHostName());


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

