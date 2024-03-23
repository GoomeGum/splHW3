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
    private BufferedOutputStream out;
    private Connections<T> connections;
    private int connectionId;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = null;
        this.connectionId = -1;
    }

    public void start(int connectionId, Connections<T> connections){
        this.connections = connections;
        this.connectionId = connectionId;
        protocol.start(connectionId, connections);
        connections.connect(connectionId, this);
        
    }


    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            out = new BufferedOutputStream(sock.getOutputStream());
            in = new BufferedInputStream(sock.getInputStream());   
            T nextMessage  = null;
            while(true){
                while(!protocol.shouldTerminate() && (read = in.read()) >= 0) {    
                    nextMessage = encdec.decodeNextByte((byte) read);
                    if(!(in.available() > 0)){
                        break;
                    }
                }
                
                if(nextMessage == null){
                    nextMessage = encdec.decodeNextByte((byte) 0);
                }
                if (nextMessage != null) {
                    try{
                        protocol.process(nextMessage);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                }
            } catch (IOException ex) {
                if(connections != null){
                    connections.disconnect(connectionId);
                    return;
                }
            }

    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
    

    @Override
    public void send(T msg) {
        try{
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
