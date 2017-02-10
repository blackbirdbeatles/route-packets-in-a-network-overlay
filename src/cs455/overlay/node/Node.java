package cs455.overlay.node;

import cs455.overlay.wireformats.Event;

/**
 * Created by MyGarden on 17/2/8.
 */
public interface Node {

    void onEvent(Event e);

}

