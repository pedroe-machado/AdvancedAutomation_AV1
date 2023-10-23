package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONObject;

public class Client {
    private Socket socket;
    private OutputStream writer;    
    private InputStream reader;

    Client(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            writer = socket.getOutputStream();            
            reader = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void SendMessage(JSONObject jsonObject) {      
        try {
            byte[] jsonBytes = jsonObject.toString().getBytes("UTF-8");
            //byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes); 
            System.out.println("sentmessage: " + jsonObject.toString());
            if(jsonBytes!=null ){
                writer.write(jsonBytes);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject Listen() throws Exception {
        byte[] message = reader.readAllBytes();
        //byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), message);
        
        JSONObject jsonObject = new JSONObject((new String(message)));

        return jsonObject;
    }
    public void Close() throws IOException {
        socket.close();
    }
}