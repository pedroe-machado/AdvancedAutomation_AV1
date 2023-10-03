package io.sim;

import de.tudresden.sumo.cmd.Vehicle;

public class Car extends Vehicle implements Runnable{

    private Cliente client;

    private float fuelTank;
    private Route currentRoute;
    private boolean on_off;

    @Override
    public void run() {
        while(this.on_off){

        }
    }

    
    
}
