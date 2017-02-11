package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.lang.*;
import java.net.SocketException;

/**
 * Created by MyGarden on 17/2/9.
 */
public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream din;

    public TCPReceiverThread(Socket socket) throws IOException{
        this.socket = socket;
        try {
            din = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }
    public void run(){

        int dataLength;
        while (socket != null){
            try{
                dataLength = din.readInt();

                byte [] data = new byte[dataLength];
                din.readFully(data,0, dataLength);


                //TODO
                //RESPONSE TO DIFFERENT MESSAGE RECEIVED



            }catch (SocketException se){
                System.out.println(se.getMessage());
                break;
            } catch (IOException ioe){
                System.out.println(ioe.getMessage());
                break;
            }

        }

    }
}
