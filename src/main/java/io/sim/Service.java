package io.sim;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;

public class Service extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Car car;

	public Service(boolean _on_off, String _idTransportService, Car car,
			SumoTraciConnection _sumo) {

		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		this.car = car;
		this.sumo = _sumo;
		this.start();
	}

	@Override
	public void run() {
		try {
			
			this.initializeRoutes();
			this.car.start();

			while (this.on_off) {
				try {
					//this.sumo.do_timestep();
				} catch (Exception e) {
				}
				Thread.sleep(this.car.getAcquisitionRate());
				if (this.getSumo().isClosed()) {
					this.on_off = false;
					System.out.println("SUMO is closed...");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeRoutes() {

		try {
			sumo.do_job_set(Route.add(this.car.getCurrenRoute().getId(), this.car.getCurrenRoute().getEdges()));
			
			sumo.do_job_set(Vehicle.addFull(this.car.getIdAuto(), 				//vehID
											this.car.getCurrenRoute().getId(), 	//routeID 
											"DEFAULT_VEHTYPE", 					//typeID 
											"now", 								//depart  
											"0", 								//departLane 
											"0", 								//departPos 
											"0",								//departSpeed
											"current",							//arrivalLane 
											"max",								//arrivalPos 
											"current",							//arrivalSpeed 
											"",									//fromTaz 
											"",									//toTaz 
											"", 								//line 
											this.car.getPersonCapacity(),		//personCapacity 
											this.car.getPersonNumber())		//personNumber
					);
			
			sumo.do_job_set(Vehicle.setColor(this.car.getIdAuto(), this.car.getColorAuto()));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public boolean isOn_off() {
		return on_off;
	}

	public void setOn(boolean _on_off) {
		this.on_off = _on_off;
	}

	public String getIdTransportService() {
		return this.idTransportService;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public Car getCar() {
		return this.car;
	}

	public io.sim.Route getRoute() {
		return this.car.getCurrenRoute();
	}
}