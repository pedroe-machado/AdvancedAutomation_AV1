package io.sim;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

public class FuelStation extends Thread {

    private static FuelStation singleStation;
    private static Semaphore semaphore;
    private static HashMap<String,Double> flowControl;

    private FuelStation(int pumps){
        semaphore = new Semaphore(pumps);
        flowControl = new HashMap<>();
    }
    public static FuelStation getInstance(int pumps){
        if(singleStation == null){
            singleStation = new FuelStation(pumps);
        }
        return singleStation;
    }

    @Override
    public void run(){
        while(true){
            escutaBanco();
        }
    }

    private void escutaBanco() {
        Socket clientSocket;
        try {
            clientSocket = new Socket("127.0.0.0", 20180);
            InputStream inputStream = clientSocket.getInputStream();
            byte[] encryptedData = inputStream.readAllBytes();
            byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(),encryptedData);

            JSONObject jsonObject = new JSONObject((new String(decryptedData)));
            inputStream.close();

            flowControl.put(jsonObject.getString("idConta"),jsonObject.getDouble("valor"));
        
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    double getValorCar(Car car){
        int attempts = 3;
        while(attempts>0){
            try {
                double pagamento = flowControl.get(car.getIdAuto());
                return pagamento;
            } catch (Exception e) {
                attempts--;
            }
        }
        System.out.println("erro ao abastecer - driver não pagou");
        return 0; 
    }
    public class FuelPumpThread extends Thread {

        private Car car;
        private float litros;

        public FuelPumpThread(Car car){
            this.car = car;
            this.litros = (float) (getValorCar(car)/5.87);
        }

        @Override
        public void run() {
            try {
                System.out.println("Carro chegou ao posto de gasolina.");
                semaphore.acquire();

                double lastSpeed = car.abastece(litros);

                System.out.println("Carro está abastecendo...");
                Thread.sleep(120000);

                car.terminaAbastecimento(lastSpeed);

                System.out.println("Carro terminou de abastecer.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }
}
