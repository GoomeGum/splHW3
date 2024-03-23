package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
public class MessageHandler {
    private String folderName = "Files";
    private AtomicBoolean isFolderLock;
    private ConcurrentHashMap<String, AtomicBoolean> files;
    
    public MessageHandler() {
        files = new ConcurrentHashMap<String, AtomicBoolean>();
        isFolderLock = new AtomicBoolean();
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
                this.files.put(filename, new AtomicBoolean());
            }
            
            AtomicBoolean isLocked = this.files.get(filename);
            
            while (!isLocked.compareAndSet(false, true)) {
                isLocked.wait();
            }
            FileInputStream file = new FileInputStream(path);
            isLocked.set(false);
            synchronized (isLocked) {
                isLocked.notifyAll();
            }
            return file;
            
        } catch (Exception e) {
            return null;
        }
    }

    public boolean handleWRQ(byte[] filenameBytes) {
        boolean addNewFlie = false;
        String filename = getFileName(filenameBytes);
        String path = folderName + "\\" + filename;
        try  
        {
            if(!this.files.containsKey(filename)){
                this.files.put(filename, new AtomicBoolean());
            }
            AtomicBoolean isLocked = this.files.get(filename);
            while(!isLocked.compareAndSet(false, true)){
                isLocked.wait();
            }
            if(!this.checkFile(filename)){
                while(!isFolderLock.compareAndSet(false, true)){
                    isFolderLock.wait();
                }
                File file = new File(path);
                file.createNewFile();
                addNewFlie = true;
                isFolderLock.set(false);
                synchronized (isFolderLock) {
                    isFolderLock.notifyAll();
                }
                
            }
            isLocked.set(false);
            synchronized (isLocked) {
                isLocked.notifyAll();
            }
        } catch (Exception e) {
        }
        return addNewFlie;
    }
    public void handleDATA(short blockNumber, byte[] data) {
        
    }
    public byte[] handleDIRQ() {
        try{

            File  folder;
            File[] listOfFiles;
            while(!isFolderLock.compareAndSet(false, true)){
                isFolderLock.wait();
            }
            folder = new File(folderName);
            listOfFiles = folder.listFiles();
            isFolderLock.set(false);
            synchronized (isFolderLock) {
                isFolderLock.notifyAll();
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
            return result;
        }
        catch(Exception e){
            return null;
        }

    }
    public boolean handleDELRQ(String filename) {
        synchronized(this){
            String path = folderName + "\\" + filename;
            File file = new File(path);
            if(file.exists()){
                file.delete();
                files.remove(filename);
                return true;
            }
            return false;
        }
    }
    public void handleBCAST(short deletedOrAdded, String filename) {}
    public void handleDISC() {}
    
    
    
    public void handleACK(short blockNumber) {}
    public void handleError(short errorCode, String errorMsg) {}
    public void handleUnknownOpcode(short opCode) {}
    public void handleLOGRQ(String username) {}
}
