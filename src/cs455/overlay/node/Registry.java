package cs455.overlay.node;

import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;

/**
 * Created by MyGarden on 17/2/8.
 */
public class Registry implements Node {

    private int port;
    private TCPServerThread tcpServerThread;


    public Registry(int port){

        this.port = port;

    }

    //response to different kind of events
    public void onEvent(Event e){

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
