package cs455.overlay.node;

import cs455.overlay.wireformats.Event;

import java.net.Socket;

/**
 * Created by MyGarden on 17/2/10.
 */
public interface Node {
    public void onEvent(Event e, Socket socket);
}
