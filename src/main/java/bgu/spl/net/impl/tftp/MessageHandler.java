package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
public class MessageHandler {
    private String folderName = "Files";
    private ConcurrentHashMap<String, Lock> files;
    public MessageHandler() {
        files = new ConcurrentHashMap<String, Lock>();
    }
    private String getFileName(byte[] fileNameBytes) {
        String fileName = "";
        for(int i=0; i<fileNameBytes.length; i++){
            fileName += (char)fileNameBytes[i];
        }
        return fileName;
    }

    private boolean checkFile(String filename) throws IOException {
        String path = folderName + "\\" + filename;
        File file = new File(path);
        return file.exists();
    }

    public FileInputStream handleRRQ(byte[] filenameBytes) {
        String filename = getFileName(filenameBytes);
        String path = folderName + "\\" + filename;
        try  {
            if (!this.checkFile(filename)) {
                return null;
            }
            if(!this.files.containsKey(filename)){
                this.files.put(filename, new ReentrantLock());
            }
            Lock lockForFile = this.files.get(filename);
            lockForFile.lock();
            FileInputStream file = new FileInputStream(path);
            lockForFile.unlock();
            return file;
            
        } catch (IOException e) {
            return null;
        }
    }

    public void handleWRQ(byte[] filenameBytes) {
        String filename = getFileName(filenameBytes);
        String path = folderName + "\\" + filename;
        try  
        {
            if(!this.files.containsKey(filename)){
                this.files.put(filename, new ReentrantLock());
            }
            Lock lockForFile = this.files.get(filename);
            lockForFile.lock();
            if(!this.checkFile(filename)){
                File file = new File(path);
                file.createNewFile();
            }
            

            lockForFile.unlock();
        } catch (IOException e) {
        }
    }
    public void handleDATA(short blockNumber, byte[] data) {
        
    }
    public byte[] handleDIRQ() {
        File  folder;
        File[] listOfFiles;
        synchronized(this){
            folder = new File(folderName);
            listOfFiles = folder.listFiles();
        }
        if(listOfFiles == null){
            return new byte[0];
        }
        Vector<Byte> files = new Vector<Byte>();
        for(File file : listOfFiles){
            for(Byte b : (file.getName() + "0").getBytes())
                files.add(b);
        }
        byte[] result =  new byte[files.size()];
        for (int i = 0; i < files.size(); i++) {
            result[i] = files.get(i);
        }
        result[result.length-1] = '\0';
        return result;

    }
    public void handleLOGRQ(String username) {}
    public void handleDELRQ(String filename) {}
    public void handleBCAST(short deletedOrAdded, String filename) {}
    public void handleDISC() {}



    public void handleACK(short blockNumber) {}
    public void handleError(short errorCode, String errorMsg) {}
    public void handleUnknownOpcode(short opCode) {}
}
