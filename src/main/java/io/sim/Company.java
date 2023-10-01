package io.sim;
import java.util.ArrayList;

public class Company implements Runnable {

    private ArrayList<Route> avaliableRoutes;
    private ArrayList<Route> runningRoutes;
    private ArrayList<Route> finishedRoutes;
    public Servidor serverCar;
    public Cliente conectaBanco;

    @Override
    public void run() {

    }

    private class BotPayment implements Runnable{

        public BotPayment( ){
            new Thread(this).start();
        }

        @Override
        public void run() {

        }
        
    }
    
}
