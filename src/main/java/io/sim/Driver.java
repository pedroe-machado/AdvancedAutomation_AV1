package io.sim;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Driver implements Runnable{
    
    private String idDriver;
    private String idConta;
    private String senha;
    private Car carro;

    private Route currentRoute;
    private ArrayList<Route> toDo;
    private ArrayList<Route> done;

    public Driver(String id, Car _car){
        this.carro = _car;
        this.idDriver = this.idConta = this.senha = id;
        new Thread(this).start();
    }

    @Override
    public void run() {

    }
    
    private class BotPayment implements Runnable{
        
        private Socket socket;
        private JSONObject jsonObject;

        public BotPayment(String idDriver) throws UnknownHostException, IOException{
            this.socket = new Socket("127.0.0.1", 20180);
            this.jsonObject = new JSONObject();
            this.jsonObject.put("idConta", idConta);
            this.jsonObject.put("senha", senha);
            this.jsonObject.put("idBeneficiario", "FuelStation");
        }

        @Override
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();
                byte[] jsonBytes = jsonObject.toString().getBytes();

                byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);
                output.write(encryptedData);

                output.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}


