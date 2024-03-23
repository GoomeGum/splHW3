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
    private volatile boolean connected;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    public void start(int connectionId, Connections<T> connections){
        protocol.start(connectionId, connections);
        connected = connections.connect(connectionId, this); // we need to connect only when a user is sending the login command
    }


    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            out = new BufferedOutputStream(sock.getOutputStream());
            in = new BufferedInputStream(sock.getInputStream());   
            while(true){
                    while(!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                        encdec.decodeNextByte((byte) read);
                        if(!(in.available() > 0)){
                            break;
                        }
                    }
                    
                    // while(!protocol.shouldTerminate()&& connected && (read = in.read()) >= 0){// read the op code
                    //     encdec.decodeNextByte((byte) read);
                    // }
                    T nextMessage = encdec.decodeNextByte((byte) 0);
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
        try{
            out.write(encdec.encode(msg));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
