package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private Connections<byte[]> connections;
    private int connectionId;
    private MessageHandler messageHandler; 
    public TftpProtocol(){

    }
    
    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connections = connections;
        this.connectionId = connectionId;
        this.messageHandler = new MessageHandler();
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
             

        }
        else if(opCode == 2){
            //WRQ
            byte[] fileName = new byte[message.length-2];
            for(int i=2; i<message.length; i++){
                fileName[i-2] = message[i];
            }
        }
        else if(opCode == 3){
            //DATA
            byte[] blockNumber = {message[2], message[3]};
            short blockNumberShort = ByteToShort(blockNumber);
            byte[] data = new byte[message.length-4];
            for(int i=4; i<message.length; i++){
                data[i-4] = message[i];
            }
        }
        else if(opCode == 4){
            //ACK
            byte[] blockNumber = {message[2], message[3]};
            short blockNumberShort = ByteToShort(blockNumber);
        }
        else if(opCode == 5){
            //ERROR
            byte[] errorCode = {message[2], message[3]};
            short errorCodeShort = ByteToShort(errorCode);
            byte[] errorMsg = new byte[message.length-4];
            for(int i=4; i<message.length; i++){
                errorMsg[i-4] = message[i];
            }
        }
        else if(opCode == 6){
            //DIRQ
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
