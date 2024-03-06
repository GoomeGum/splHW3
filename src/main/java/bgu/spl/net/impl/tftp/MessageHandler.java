package bgu.spl.net.impl.tftp;

public class MessageHandler {
    public MessageHandler() {
    }

    public void handleRRQ(String filename) {
       
    }

    public void handleWRQ(String filename) {
      
    }
    public void handleDATA(short blockNumber, byte[] data) {}
    public void handleACK(short blockNumber) {}
    public void handleError(short errorCode, String errorMsg) {}
    public void handleUnknownOpcode(short opCode) {}
    public void handleDIRQ() {}
    public void handleLOGRQ(String username) {}
    public void handleDELRQ(String filename) {}
    public void handleBCAST(short deletedOrAdded, String filename) {}
    public void handleDISC() {}
}
