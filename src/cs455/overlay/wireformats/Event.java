package cs455.overlay.wireformats;


import java.io.IOException;

/**
 * Created by MyGarden on 17/2/11.
 */
public interface Event {
    public byte[] getBytes() throws IOException;
    public static Event decodebyte(byte[] marshalledBytes) throws IOException{
        return null;
    };
}
