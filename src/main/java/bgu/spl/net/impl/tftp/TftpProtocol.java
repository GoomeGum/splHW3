package bgu.spl.net.impl.tftp;
import java.io.FileInputStream;
import java.io.IOException;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.impl.Pair;
import java.util.concurrent.LinkedBlockingQueue;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private Connections<byte[]> connections;
    private int connectionId;
    private MessageHandler messageHandler; 
    private LinkedBlockingQueue<Pair<Short, Object>> reqs;
    private boolean shouldTerminate = false;
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
    private void sendError(){
        byte[] error = {0, 5, 0, 0};
        this.connections.send(connectionId, error);
    }

    @Override
    public void process(byte[] message) {
        byte[] opCodebyts = {message[0], message[1]};
        short opCode = ByteToShort(opCodebyts);
        int opCodeInt = Integer.valueOf(opCode);
    
        if(opCodeInt == OpCodes.RRQ){
            //RRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
            byte[] file;
            try {
                FileInputStream fileInputStream = this.messageHandler.handleRRQ(fileName);
                if(fileInputStream == null){
                    this.sendError();
                    return;
                }
                file = fileInputStream.readAllBytes();
                this.connections.send(connectionId,file);   
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        else if(opCodeInt == OpCodes.WRQ){
            //WRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
            boolean addedFile = this.messageHandler.handleWRQ(fileName);
            this.reqs.add(new Pair<Short, Object>(opCode, fileName));
            this.sendAck();
            if(addedFile){
                byte[] msg = new byte[fileName.length+5];
                byte[] initM = "add".getBytes();
                for(int i =0; i<initM.length; i++){
                    msg[i] = initM[i];
                }
                msg[initM.length] = " ".getBytes()[0]; // add space 
                for(int i =0; i<fileName.length; i++){
                    msg[i+initM.length+1] = fileName[i];
                }
                this.connections.sendBrodcast(msg);
            }
        }
        else if(opCodeInt == OpCodes.DATA){
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
            return;
        }
        else if(opCodeInt == OpCodes.ACK){
           //ACK
            System.out.println("rcv - ACK");            
        }
        else if(opCode == OpCodes.ERROR){
            //ERROR
            System.out.println("rcv - ERROR");
        }
        else if(opCodeInt == OpCodes.DIRQ){ 
            //DIRQ
            connections.send(connectionId , this.messageHandler.handleDIRQ());
            return;
        }
        else if(opCodeInt == OpCodes.LOGRQ){
            //LOGRQ
            byte[] userName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                userName[i-2] = message[i];
            }
            
        }
        else if(opCodeInt == OpCodes.DELRQ){
            //DELRQ
            String filename = new String(message, 2, message.length-2);  
            if(this.messageHandler.handleDELRQ(filename)){
                this.sendAck();
                return;
            }
            this.sendError();
            return;
        }
        else if(opCodeInt == OpCodes.BCAST){
            //BCAST
            byte[] initM = message[2] == 0? "del".getBytes() : "add".getBytes();
            byte[] fileName = new byte[message.length-3];
            for(int i=3; i<message.length; i++){
                fileName[i-3] = message[i];
            }
            byte[] msg = new byte[initM.length + fileName.length + 1];
            for(int i=0; i<initM.length; i++){
                msg[i] = initM[i];
            }
            for(int i=0; i<fileName.length; i++){
                msg[i+initM.length] = fileName[i];
            }
            msg[msg.length -1] = 0; // make sure its not overiding the last byte
            if(!connections.sendBrodcast(msg)){
                System.out.println("failed to send brodcast");
            }
        }
        else if(opCode == OpCodes.DISC){
            //DISC 
            connections.disconnect(connectionId);
            this.shouldTerminate = true;
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
        return this.shouldTerminate;
    } 


    
}
