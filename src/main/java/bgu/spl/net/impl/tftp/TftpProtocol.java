package bgu.spl.net.impl.tftp;
import java.io.IOException;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.OpCodesEnum;
import bgu.spl.net.impl.Pair;
import java.util.concurrent.LinkedBlockingQueue;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private Connections<byte[]> connections;
    private int connectionId;
    private MessageHandler messageHandler; 
    private LinkedBlockingQueue<Pair<Short, Object>> reqs;
    public TftpProtocol(){
        this.messageHandler = new MessageHandler();
        this.reqs = new LinkedBlockingQueue<Pair<Short, Object>>();
    }

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
    }
    private void sendAck(short blockNumber){
        byte[] blockNumberBytes = ShortToByte(blockNumber);
        byte[] ack = {0, 4, blockNumberBytes[0], 0};//Todo check if the block number is correct
        this.connections.send(connectionId, ack);
    }
    private void sendAck(){
        byte[] ack = {0, 4, 0, 0};
        this.connections.send(connectionId, ack);
    }

    @Override
    public void process(byte[] message) {
        byte[] opCodebyts = {message[0], message[1]};
        short opCode = ByteToShort(opCodebyts);
        if(opCode == 1){
            //RRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
            byte[] file;
            try {
                file = this.messageHandler.handleRRQ(fileName).readAllBytes();
                this.connections.send(connectionId,file);   
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        else if(opCode == 2){
            //WRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
            this.messageHandler.handleWRQ(fileName);
            this.reqs.add(new Pair<Short, Object>(opCode, fileName));
            this.sendAck();
        }
        else if(opCode == 3){
            //DATA
            byte[] packetSize = {message[2], message[3]};
            byte[] blockNumber = {message[4], message[5]};
            short packetSizeShort = ByteToShort(packetSize);
            short blockNumberShort = ByteToShort(blockNumber);

            byte[] data = new byte[message.length-6];
            for(int i = 6; i<message.length; i++){ 
                data[i-6] = message[i];
            }
            Pair<Short,Object> request =  reqs.peek();
            if(request != null){
                if(request.getFirst() == 2){
                    this.messageHandler.handleDATA(blockNumberShort, data);
                    this.sendAck(blockNumberShort);
                }
                if(packetSizeShort < 512){
                    reqs.poll();
                }
            }
            else{
                throw new IllegalArgumentException("received data without command");
            }
        }
        else if(opCode == 4){
           //ACK
            System.out.println("rcv - ACK");            
        }
        else if(opCode == 5){
            //ERROR
            System.out.println("rcv - ERROR");
        }
        else if(opCode == 6){ 
            //DIRQ
            connections.send(connectionId , this.messageHandler.handleDIRQ());
        }
        else if(opCode == 7){
            //LOGRQ
            byte[] userName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                userName[i-2] = message[i];
            }
            
        }
        else if(opCode == 8){
            //DELRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
        }
        else if(opCode == 9){
            //BCAST
            byte[] delOrAdd = {message[2]};
            byte[] fileName = new byte[message.length-3];
            for(int i=3; i<message.length; i++){
                fileName[i-3] = message[i];
            }
        }
        else if(opCode == 10){
            //DISC 
            connections.disconnect(connectionId);
        }


    }
    private short ByteToShort(byte[] bytes){
    
        return ( short ) ((( short ) bytes [0]) << 8 | ( short ) ( bytes [1]));
        
    }
    private byte[] ShortToByte(short a){
        
        return new byte []{( byte ) ( a >> 8) , ( byte ) ( a & 0xff ) };
    }
    @Override
    public boolean shouldTerminate() {
        // TODO implement this
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    } 


    
}
