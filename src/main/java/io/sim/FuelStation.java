package io.sim;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import io.sim.Car;

public class FuelStation implements Runnable {

    private static Semaphore semaphore = new Semaphore(2);

    @Override
    public void run(){
        while(true){

        }
    }

    public class FuelPumpThread extends Thread {

        private Car car;
        private float litros;

        public FuelPumpThread(Car car, double valor){
            this.car = car;
            this.litros = (float) (valor/5.87);
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
