package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private volatile boolean connected = true;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    public void start(int connectionId, Connections<T> connections){
        protocol.start(connectionId, connections);
        connections.connect(connectionId, this);
    }


    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            
            in = new BufferedInputStream(sock.getInputStream());
            while(!protocol.shouldTerminate()&& connected && (read = in.read()) >= 0){
                encdec.decodeNextByte((byte) read);
            }
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                if(read == 0){
                    break;
                }
                encdec.decodeNextByte((byte) read);
                
            }
            T nextMessage = encdec.decodeNextByte((byte) 0);
            if (nextMessage != null) {
                try{
                    protocol.process(nextMessage);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        //IMPLEMENT IF NEEDED
    }
}
