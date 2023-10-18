package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Car extends Thread{

    private boolean needFuel;
    private float fuelTank;
    private Route currentRoute;
    private SumoTraciConnection sumo;

	private Auto auto;
    private String idAuto;
    private float acquisitionRate;
	
	public Car(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection sumo, long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) throws Exception {

        new AskRoute();
        
        this.auto = new Auto(_on_off, _idAuto, _colorAuto, _driverID, sumo, _acquisitionRate, _fuelType,_fuelPreferential, _fuelPrice, _personCapacity, _personNumber);
        this.fuelTank = 10;
        this.sumo = sumo;
        this.idAuto = _idAuto;
        this.acquisitionRate = (float) _acquisitionRate;
	}
    
    @Override
    public void run() {
        auto.start();
        while(true){
            try {
                wait();
                this.fuelTank -= (750000)/(((float)sumo.do_job_get(Vehicle.getFuelConsumption(idAuto)))*(acquisitionRate/1000)); // conversão mg/s -> L
                new SendInfo().start();
                if(fuelTank<=3){
                    needFuel = true;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Métodos de abastecimento - somente FuelStation acessa
    public double abastece(float litros) throws Exception{
        double lastSpeed = (double) sumo.do_job_get(Vehicle.getSpeed(idAuto));
        sumo.do_job_set(Vehicle.setSpeed(idAuto, lastSpeed));
        fuelTank += litros;
        return lastSpeed;
    }
    public void terminaAbastecimento(double speed) throws Exception{
        sumo.do_job_set(Vehicle.setSpeed(idAuto, speed));
    }

    // Comunicação com a Company e RouteHandler
    private class AskRoute extends Thread {

        private Socket socket;
        private JSONObject jsonFlag;
        private Route newRoute;

        public AskRoute() throws UnknownHostException, IOException {
            this.socket = new Socket("127.0.0.1", 20181);
            this.jsonFlag = new JSONObject();
            this.jsonFlag.put("servico", "REQUEST_ROUTE");
            this.start();
        }

        @Override
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();
                byte[] jsonBytes = jsonFlag.toString().getBytes();
                byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(),
                        jsonBytes);
                output.write(encryptedData);
                output.close();

                Thread.sleep(10);
                InputStream input = socket.getInputStream();
                byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(),
                        input.readAllBytes());
                Object obj = new JSONParser().parse(new String(decryptedData));
                JSONObject jsonPackage = (JSONObject) obj;

                input.close();
                socket.close();

                this.newRoute = new Route((String) jsonPackage.get("idRota"),
                        (SumoStringList) jsonPackage.get("edges"));
                currentRoute = newRoute;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public Route getRoute() {
            return this.newRoute;
        }
    }
    
    private class SendInfo extends Thread {
        private Socket socket;
        private JSONObject jsonFlag;
        private Route newRoute;

        public SendInfo() throws UnknownHostException, IOException{
            this.socket = new Socket("127.0.0.1", 20181);
            this.jsonFlag = new JSONObject();
            this.jsonFlag.put("servico","SEND_INFO");
            this.jsonFlag.put("idAuto", idAuto);
            this.jsonFlag.put("fuelTank", fuelTank);
            this.start();
        }

        @Override
        public void run() {
            while(auto.isOn_off()){
                try {                
                    OutputStream output = socket.getOutputStream();
                    byte[] jsonBytes = jsonFlag.toString().getBytes();
                    byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);
                    output.write(encryptedData);
                    output.close();
                } catch (Exception e){}
            }
        }

    }
    
}
