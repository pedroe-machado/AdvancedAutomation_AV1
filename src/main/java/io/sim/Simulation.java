package io.sim;

import java.io.IOException;
import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Simulation extends Thread{

    private SumoTraciConnection sumo;
    private Company company;
    private AlphaBank alphaBank;
    private FuelStation fuelStation;

    public Simulation(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new PeriodicTask(), 5000, 500, TimeUnit.MILLISECONDS);     

		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
        
        // Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

        this.start();
    }

	public void run() {
		try {
			sumo.runServer(12345);

			company = new Company(sumo);

			ArrayList<String> users = company.getCLTs(); //Cria todas as contas no banco
			users.add("company");
			users.add("fuelStation");

			alphaBank = new AlphaBank(users);

			TransportService tS1 = new TransportService(true, "CAR1", i1, a1, sumo);
			tS1.start();
			Thread.sleep(5000);
			a1.start();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private class PeriodicTask implements Runnable {
		@Override
		public void run() {
			try {
				sumo.do_timestep();
			} catch (Exception e) {
				System.out.println("erro timestep");
				e.printStackTrace();
			}
		}
	}
}
