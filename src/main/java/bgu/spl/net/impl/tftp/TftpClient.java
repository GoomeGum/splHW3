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
                String msg =  System.console().readLine();
                String[] splited = msg.split(" ");
                byte[] msgBytes = null;
                String command = splited[0];
                String data;
                if(splited.length > 1){
                    data = "";
                    for (int i = 1; i < splited.length; i++) {
                        data += splited[i] + " ";
                    }
                }
                else{
                    data = null;
                }
                byte[] opcode = encdec.encodeOpCode(command);
                if(data != null){
                    byte[] dataBytes = encdec.encode(data);
                    msgBytes = new byte[opcode.length + dataBytes.length];
                    for (int i = 0; i < opcode.length; i++) {
                        msgBytes[i] = opcode[i];
                    }
                    for (int i = 0; i < dataBytes.length; i++) {
                        msgBytes[i + opcode.length] = dataBytes[i];
                    }

                }
                else{
                    msgBytes = opcode;
                }
                System.out.println("sending message to server");
                out.write(msgBytes);
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
                        break;
                    }
                }
                System.out.println("message from server: " + line);
        }
        catch (Exception e){
            e.printStackTrace();
            break;
        }
    }
        sock.close();
}
    
}
