package io.sim;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import it.polito.appeal.traci.SumoTraciConnection;

public class Driver implements Runnable{
    
    private String idDriver;
    private String idConta;
    private String senha;
    private Car carro;
    private SumoTraciConnection sumo;
    private Service currentService;
    private Route currentRoute;
    private ArrayList<Route> toDo;
    private ArrayList<Route> done;

    public Driver(SumoTraciConnection sumo, String id, Car _car){
        this.sumo = sumo;
        this.toDo = new ArrayList<>();
        this.done = new ArrayList<>(); 
        this.carro = _car;
        this.idDriver = this.idConta = this.senha = id;
        new Thread(this).start();
    }
    @Override
    public void run() {
        while (carro.isAlive()) {
            if(carro.theresNewRoute()){
                try {
                    done.add(Integer.parseInt(currentRoute.getId()), currentRoute);
                    currentService.setOn(false);
                } catch (Exception e) {}
                currentRoute = carro.getCurrenRoute();
                carro.ackNewRoute();
                currentService = new Service(true, idConta, carro, sumo);
            }
            while (!carro.doesNeedFuel() && !carro.theresNewRoute()) {
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("driver sleep error");
                }
            }
            try {
                if(carro.doesNeedFuel()){
                    carro.abastecendo(true);
                    //Thread fuelStation
                    new BotPayment(idDriver);
                }
            } catch (UnknownHostException e) {
                System.out.println("driver-bank connection");
            } catch (IOException e) {
                System.out.println("driver payment error");
                e.printStackTrace();
            }
        }
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
            new Thread(this).start();
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

    public String getIdDriver() {
        return this.idDriver;
    }
}


