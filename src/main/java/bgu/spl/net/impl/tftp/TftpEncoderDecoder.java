package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        if(nextByte == 0) {
            return popString();
        }
        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        byte[] result = new byte[message.length + 1];
        System.arraycopy(message, 0, result, 0, message.length);
        result[message.length] = 0;
        return result;

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