package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;


public class TftpClient {
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            args = new String[]{"localhost"};
        }
        TftpClientEncoderDecoder encdec = new TftpClientEncoderDecoder();
        Socket sock = new Socket(args[0], 7777);    
        BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
        BufferedInputStream in = new BufferedInputStream(sock.getInputStream());   
        
        System.out.println("connected to server");
        while(true){
            try {
                byte[] msg = new byte[2];
                msg[0] = 0;
                msg[1] = 6;
                System.out.println("sending message to server");
                out.write(msg);
                out.flush();
    
                System.out.println("awaiting response");
              
                String line = "";
                int read;
                byte[] result = null;
                while(true) {
                    while((read = in.read()) >= 0) {
                        result  = encdec.decodeNextByte((byte) read);
                        
                        if(result != null){
                            break;
                        }
                    }
                if(result != null){
                    for (int i = 0; i < result.length; i++) {
                        char c = result[i] != 48? (char)result[i]: ' ' ;
                        line += c;
                }
            }
                    
                
                
                System.out.println("message from server: " + line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            break;
        }
    }
        sock.close();
}
    
}
