package cs455.overlay.transport;

import java.net.Socket;

/**
 * Created by MyGarden on 17/2/9.
 */
public class TCPReceiverThread {
    Socket socket;
    public TCPReceiverThread(Socket socket){
        this.socket = socket;
    }
}
