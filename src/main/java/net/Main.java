// package net;

// import java.io.BufferedReader;
// import java.io.InputStreamReader;

// import io.sim.Client;
// import io.sim.Company;
// import io.sim.Service;

// public class Main {
//     public static void main(String[] args) {
//         Service comp = new Company(3000);
//         comp.start();

//         String message = "";
//         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
//         try {
//             Thread.sleep(1000); // 1000 milliseconds = 1 second
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//         }

//         Client client = new Client("127.0.0.1", 3000);

//         while (!message.equals("STOP")) {
//             try {
//                 System.out.println("Enter message:");
//                 message = reader.readLine();
//                 client.SendMessage(message);
//                 String mesString = client.Listen();
//                 System.out.println("[client] received: " + mesString);
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }
