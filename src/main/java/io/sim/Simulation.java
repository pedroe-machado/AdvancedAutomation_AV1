package io.sim;

import java.io.IOException;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

public class Simulation extends Thread{

    private SumoTraciConnection sumo;
    private Company company;
    private AlphaBank alphaBank;
    private FuelStation fuelStation;


    public Simulation(){
        
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
        
        // Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

        this.start();
    }

    public void run(){
	
        company = new Company(sumo);

		try {
			sumo.runServer(12345);



			if (i1.isOn()) {

				// fuelType: 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
				int fuelType = 2;
				int fuelPreferential = 2;
				double fuelPrice = 3.40;
				int personCapacity = 1;
				int personNumber = 1;
				SumoColor green = new SumoColor(0, 255, 0, 126);
				Auto a1 = new Auto(true, "CAR1", green,"D1", sumo, 500, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber);
				TransportService tS1 = new TransportService(true, "CAR1", i1, a1, sumo);
				tS1.start();
                Thread.sleep(5000);
				a1.start();
			}
		
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

}
