package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

/**
 * Created by MyGarden on 17/2/8.
 */
public class Registry implements Node {

    private int port;
    private TCPServerThread tcpServerThread;
    private HashMap<String, Integer> registeredNodeList;                     //think of what kind of data structure is the best choice

    public Registry(int port){

        this.port = port;
        registeredNodeList = new HashMap<>();
    }

    public String toString(){
        return "Registry";
    }



    private boolean isIdenticalIP(String IP, String realIP){
        if (realIP.equals("127.0.0.1")){
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
    private int isValidRegistration(String IP, String realIP,int port){
        boolean isIdenticalHost = isIdenticalIP(IP, realIP);
        if (isIdenticalHost && !registeredNodeList.containsKey(IP+":"+String.valueOf(port)))
            return 0;
        if (!isIdenticalHost)
            return 1;
        if (isIdenticalHost && registeredNodeList.containsKey(IP+":"+String.valueOf(port)))
            return -1;

        return -2;
    }



    //return  0    valid Deregister
    //return  1    fail: IP is not real
    //return  -1   fail: could not deregister before registering
    private int isValidDeregistration(String IP, String realIP,int port){
        boolean isIdenticalHost = isIdenticalIP(IP, realIP);
        if (isIdenticalHost && registeredNodeList.containsKey(IP+":"+String.valueOf(port)))
            return 0;
        if (!isIdenticalHost)
            return 1;
        if (isIdenticalHost && !registeredNodeList.containsKey(IP+":"+String.valueOf(port)))
            return -1;

        return -2;
    }

    private void registerProcess(Register register, Socket socket){

        System.out.println("Received register event from " + socket.getRemoteSocketAddress().toString());

        String IP = register.getIP();
        String realIP = socket.getInetAddress().getHostAddress();
        int port = register.getPort();

        //to prevent multi-threads access the registeredNodeList at the same time
        synchronized (this.registeredNodeList) {
            int isValid = isValidRegistration(IP,realIP,port );

            //if register succeed, send success response
            if (isValid == 0) {
                //add the current node to registeredNodeList
                registeredNodeList.put(IP+ ":" +String.valueOf(port), 1);

                //send response packet to the messaging node
                    String info = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodeList.size() + ")";
                    RegisterResponse registerResponse = new RegisterResponse( true, info);
                    try {
                        TCPSender.sendData(registerResponse.getBytes(), socket);
                    }
                    catch (IOException ioe){
                        System.out.println("Failed to Marshall the RegisterResponse. Exit now.");

                        //TODO: SEND A "INTERUPT" EVENT TO REGISTRY AND DELETE THE MEMBERSHIP FOR THIS NODE

                        System.exit(-1);
                    }


            }

            //if registering fail, send failure response
            else{
                String info;
                if (isValid == 1)
                    info = "Fail to Register: IP is not real";
                else
                    if (isValid == -1)
                        info = "Fail to Register: IP already registered";
                    else
                        info = "The value of isValid is -2";
                RegisterResponse registerResponse = new RegisterResponse( false, info);
                try {
                    TCPSender.sendData(registerResponse.getBytes(), socket);
                } catch (IOException ioe){
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");
                    System.exit(-1);
                }
            }
        }

    };

    private void deregisterProcess(Deregister deregister, Socket socket){

        System.out.println("Received deregister event from " + socket.getRemoteSocketAddress().toString());

        String IP = deregister.getIP();
        int port = deregister.getPort();
        String realIP = socket.getInetAddress().getHostAddress();

        //to prevent multi-threads access the registeredNodeList at the same time
        synchronized (this.registeredNodeList) {
            //The valid standard for deregister is the same as register
            int isValid = isValidDeregistration(IP,realIP,port );

            //if deregister succeed, send success response
            if (isValid == 0) {
                //delete the current node from registeredNodeList
                registeredNodeList.remove(IP+ ":" +String.valueOf(port));

                //send success response packet to the messaging node
                String info = "Deregistration request successful. The number of messaging nodes currently constituting the overlay is (" + registeredNodeList.size() + ")";
                DeregisterResponse deregisterResponse = new DeregisterResponse( true, info);
                try {
                    TCPSender.sendData(deregisterResponse.getBytes(), socket);
                }
                catch (IOException ioe){
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");

                    //TODO: SEND A "INTERUPT" EVENT TO REGISTRY AND DELETE THE MEMBERSHIP FOR THIS NODE

                    System.exit(-1);
                }


            }

            //if deregistering fail, send failure response
            else{
                String info;
                if (isValid == 1)
                    info = "Fail to Deregister: IP is not real";
                else
                if (isValid == -1)
                    info = "Fail to Deregister: could not deregister before registering";
                else
                    info = "The value of isValid is -2";
                DeregisterResponse deregisterResponse = new DeregisterResponse( false, info);
                try {
                    TCPSender.sendData(deregisterResponse.getBytes(), socket);
                } catch (IOException ioe){
                    System.out.println("Failed to Marshall the RegisterResponse. Exit now.");
                    System.exit(-1);
                }
            }
        }

    };



    //response to different kind of events
    public void onEvent(Event event, Socket socket){
        switch (event.getType()){
            case REGISTER_REQUEST:
                registerProcess((Register)event, socket);
                break;
            case DEREGISTER_REQUEST:
                deregisterProcess((Deregister)event, socket);
                break;
        }
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







    }
}
