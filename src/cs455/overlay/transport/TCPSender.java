package cs455.overlay.transport;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by MyGarden on 17/2/9.
 */
/*
public class TCPSender {

    private Socket socket;
    private DataOutputStream dout;

    public TCPSender(Socket socket) throws IOException{
        this.socket = socket;
        dout = new DataOutputStream(socket.getOutputStream());
    }


    public void sendData(byte[] dataToSend) throws IOException {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend,0,dataLength);
        dout.flush();
    }
}
*/

public class TCPSender {

    public static void sendData(byte[] dataToSend, Socket socket) {
        try {
            //to prevent multi-threads use the same socket between two nodes (e.g task1: A relays packets to B, task2: A sends packets to B directly)
            synchronized (socket) {
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                int dataLength = dataToSend.length;
                dout.writeInt(dataLength);
                dout.write(dataToSend, 0, dataLength);
                dout.flush();
            }
        } catch (IOException ioe){
            System.out.println("Exception: Fail to send message.");
            System.exit(-1);
            //TODO: here need to judge the parent of this sender. For registry: delete the msgNode. For MsgNode: Nothing.

        }
    }
}