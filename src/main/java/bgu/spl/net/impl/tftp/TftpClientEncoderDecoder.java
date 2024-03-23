package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpClientEncoderDecoder {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;

    
    public byte[] decodeNextByte(byte nextByte) {
        if(len == 0 && nextByte == 0){
            pushByte(nextByte);
            return null;
        }
        if(nextByte == 0) {
            return popString();
        }
        pushByte(nextByte);
        return null;
    }

    public byte[] encode(String data) {
        byte[] result = new byte[data.length() + 1];
        for (int i = 0; i < data.length(); i++) {
            result[i] = (byte) data.charAt(i);
        }
        result[data.length()] = 0;
        return result;
        
    }
    
    public byte[] encodeOpCode(String message) {
        byte[] opcode = new byte[2];
        opcode[0] = 0;
        if(message.equals("RRQ")){
            opcode[1] = 1;
        }
        else if(message.equals("WRQ")){
            opcode[1] = 2;
        }
        else if(message.equals("DATA")){
            opcode[1] = 3;
        }
        else if(message.equals("ACK")){
            opcode[1] = 4;
        }
        else if(message.equals("ERROR")){
            opcode[1] = 5;
        }
        else if(message.equals("DIRQ")){
            opcode[1] = 6;
        }
        else if(message.equals("LOGRQ")){
            opcode[1] = 7;
        }
        else if(message.equals("DELRQ")){
            opcode[1] = 8;
        }
        else if(message.equals("BCAST")){
            opcode[1] = 9;
        }
        else if(message.equals("DISC")){
            opcode[1] = 10;
        }
        return opcode;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = java.util.Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private byte[] popString() {
        byte[] result = java.util.Arrays.copyOf(bytes, len);
        len = 0;
        return result;
    }
    
}
