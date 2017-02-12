package cs455.overlay.transport;

import java.io.IOException;
import java.net.*;

/**
 * Created by MyGarden on 17/2/9.
 */
public class TCPServerThread extends Thread{

    private ServerSocket serverSocket;


    public TCPServerThread(int port){
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException ioe){
            System.out.println(ioe.getMessage());

        }
    }
    public void run(){

        System.out.println("Waiting for client on port " + Integer.toString(serverSocket.getLocalPort()) + "..." );
        while (true){
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Just connected to " + socket.getRemoteSocketAddress());
                TCPReceiverThread tcpReceiverThread = new TCPReceiverThread(socket);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }
    }

    public int getListeningPort(){
        return serverSocket.getLocalPort();
    }
    public String getHostName() throws IOException{
        InetAddress IP = InetAddress.getLocalHost();
        return IP.toString();
    }
}
