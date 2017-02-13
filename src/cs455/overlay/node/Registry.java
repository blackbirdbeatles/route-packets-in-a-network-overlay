package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.Type;

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

    private boolean isValidRegistration(String IP, String realIP){
        boolean result = false;
        if (IP.equals(realIP) && registeredNodeList.containsKey(IP))
            result = true;
        return result;
    }

    private void registerProcess(Register register, Socket socket){

        System.out.println("Received register event from " + socket.getRemoteSocketAddress().toString());

        String IP = register.getIP(register);
        String realIP = socket.getRemoteSocketAddress().toString();
        int port = register.getPort(register);

        //to prevent multi-threads access the registeredNodeList at the same time
        synchronized (this.registeredNodeList) {
            if (isValidRegistration(IP,realIP )) {
                //add the current node to registeredNodeList
                registeredNodeList.put(IP, port);

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
            else{
                String info = "Registration request failed";
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

    //response to different kind of events
    public void onEvent(Event event, Socket socket){
        switch (event.getType()){
            case REGISTER_REQUEST:
                registerProcess((Register)event, socket);
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
