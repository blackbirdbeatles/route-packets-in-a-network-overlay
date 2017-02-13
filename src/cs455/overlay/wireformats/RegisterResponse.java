package cs455.overlay.wireformats;

import java.io.*;

/**
 * Created by MyGarden on 17/2/12.
 */
public class RegisterResponse implements Event{

    private Type type;
    private boolean code;
    private String info;

    public RegisterResponse(boolean code, String info){

        this.type = Type.REGISTER_RESPONSE;
        this.code = code;
        this.info = info;

    }



    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //write type
        dout.writeInt(type.getValue());

        //write code (boolean)
        dout.writeBoolean(code);

        //write info string
        byte[] infoBytes = info.getBytes();
        int infoLength = infoBytes.length;
        dout.writeInt(infoLength);
        dout.write(infoBytes);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    public static RegisterResponse decodeByte(byte[] marshalledBytes) throws IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //readin type, but actually already known
        din.readInt();

        //readin code(boolean)
        boolean code = din.readBoolean();

        //readin info string
        int infoLength = din.readInt();
        byte[] infoBytes = new byte[infoLength];
        din.readFully(infoBytes);
        String info = new String(infoBytes);



        baInputStream.close();
        din.close();

        return new RegisterResponse(code,info);


    }
    public Type getType(){
        return this.type;
    }
}
