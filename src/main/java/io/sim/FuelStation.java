package main.java.io.sim;

import java.util.Queue;
import java.util.concurrent.Semaphore;
import io.sim.Car;

public class FuelStation implements Runnable {

    public static void abastece(Car car, double valor){
        new FuelPumpThread(car, valor).start();
    }

    public class FuelPumpThread extends Thread {
        private static Semaphore semaphore = new Semaphore(2);

        private Car car;
        private float litros;

        public FuelPumpThread(Car car, double valor){
            this.car = car;
            this.litros = (float) valor/5.87;
        }

        public void run() {
            try {
                System.out.println("Carro chegou ao posto de gasolina.");
                semaphore.acquire();

                double lastSpeed = car.abastece(litros); //abastece e salva velocidade anterior

                System.out.println("Carro está abastecendo...");
                Thread.sleep(120000); // 2 minutos em milissegundos

                car.terminaAbastecimento(lastSpeed);

                System.out.println("Carro terminou de abastecer.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }
}
