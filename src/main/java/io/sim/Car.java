package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim.traci4j.src.java.it.polito.appeal.traci.SumoTraciConnection;
import sim.traci4j.src.java.it.polito.appeal.traci.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import netscape.javascript.JSObject;

public class Car extends Vehicle implements Runnable{

    private float fuelTank;
    private Route currentRoute;

	private Auto auto;

	private ArrayList<DrivingData> drivingRepport;
	
	public Car(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber) throws Exception {
        
        new AskRoute().start();
        this.auto = new Auto(_on_off, _idAuto, _colorAuto, _driverID, _sumo, _acquisitionRate, _fuelType, _fuelPreferential, _fuelPrice, _personCapacity, _personNumber, this);

        this.fuelTank = 10;
	}
    
    @Override
    public void run() {
        
        while(true){

        }
    }

    // Método que Auto atualiza repport
    public void getRepport(DrivingData _repport){
        this.drivingRepport.add(_repport);
    }

    // Métodos de abastecimento - somente FuelStation acessa
    public double abastece(float litros) throws IOException{
        double lastSpeed = this.getSpeed();
        this.changeSpeed((double) 0);
        fuelTank += litros;
        return lastSpeed;
    }
    public void terminaAbastecimento(double speed) throws IOException{
        this.changeSpeed(speed);
    }

    // Comunicação com a Company e RouteHandler
    private class AskRoute extends Thread {

        private Socket socket;
        private JSONObject jsonFlag;
        private Route newRoute;

        public AskRoute() throws UnknownHostException, IOException{
            this.socket = new Socket("127.0.0.1", 20181);
            this.jsonFlag = new JSONObject();
            this.jsonFlag.put("servico", "REQUEST_ROUTE");
            this.start();
        }

        @Override
        public void run() {
            try {
                
                OutputStream output = socket.getOutputStream();
                byte[] jsonBytes = jsonFlag.toJSONString().getBytes();
                byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);
                output.write(encryptedData);
                output.close();
                
                Thread.sleep(10);
                InputStream input = socket.getInputStream();
                byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(),input.readAllBytes());
                Object obj = new JSONParser().parse(new String(decryptedData));
                JSONObject jsonPackage = (JSONObject) obj;

                input.close();
                socket.close();

                this.newRoute = new Route((String)jsonPackage.get("idRota"), (SumoStringList)jsonPackage.get("edges"));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public Route getRoute(){
            return this.newRoute;
        }
    }
    
    private class SendInfo extends Thread {

    }
    
}
