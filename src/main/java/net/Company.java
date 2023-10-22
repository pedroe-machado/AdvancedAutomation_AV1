// package net;

// import java.net.Socket;

// import io.sim.Service;

// public class Company extends Service {

//     Company(int port) {
//         super(port);
//     }

//     public int calcPreco(int peso, int distancia) {
//         return peso * distancia;
//     }

//     @Override
//     public Server CreateServerThread(Socket conn) {
//         return new CompanyServer(conn);
//     }

//     public class CompanyServer extends Server {
//         public CompanyServer(Socket conn) {
//             super(conn);
//         }

//         @Override
//         protected void ProcessMessage(String message) {
//             System.out.println("[company] received: " + message);
//             SendMessage("MESSAGE RECEIVED");
//         }
//     }
// }
