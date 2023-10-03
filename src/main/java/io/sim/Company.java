package io.sim;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.core.jmx.Server;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Company implements Runnable {
    public static String uriRoutesXML = "\\map\\map.rou.xml";

    private ArrayList<Route> avaliableRoutes;
    private ArrayList<Route> runningRoutes;
    private ArrayList<Route> finishedRoutes;

    public Company(){
        reconstructOriginalFile(); //testar se xml já foi limpo
        new Thread(this).start();
    }

    @Override
    public void run() {

        CreateRoutes createRoutes = new CreateRoutes();
            createRoutes.start();
            try {
                createRoutes.join();
                this.avaliableRoutes = createRoutes.getRoutes();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        
        while(true){



            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Gera rotas e processa arquivos raiz
    private class CreateRoutes extends Thread{
        private ArrayList<Route> routes;

        @Override
        public void run(){
            this.routes = parseRoutes();
            limpaXml();
        }

        private ArrayList<Route> parseRoutes() {
            
            ArrayList<Route> routesList = new ArrayList<>();
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(Company.uriRoutesXML);
                NodeList nList = doc.getElementsByTagName("vehicle");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element elem = (Element) nNode;
                        String id = elem.getAttribute("id");
                        Node node = elem.getElementsByTagName("route").item(0);
                        Element edges = (Element) node;
                        String[] edgesArray = edges.getAttribute("edges").split(" ");
                        ArrayList<String> edgesList = new ArrayList<>();
                        for (String edge : edgesArray) {
                            edgesList.add(edge);
                        }
                        routesList.add(new Route(id, edgesList));
                    }
                }

            } catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }

            return routesList;
        }
        private void limpaXml(){
            try {
                FileWriter fileWriter = new FileWriter(Company.uriRoutesXML);
                fileWriter.write("");
                fileWriter.close();

                FileWriter tempFileWriter = new FileWriter("\\data\\temp.xml");
                tempFileWriter.write(""); 
                tempFileWriter.close();
                BufferedWriter writer = new BufferedWriter(new FileWriter("\\data\\temp.xml"));

                for (Route route : avaliableRoutes) {
                    writer.write("<vehicle id=\"" + route.getId() + "\" depart=\"0.00\">\n");
                    writer.write("  <route edges=\"" + String.join(" ", route.getEdges()) + "\"/>\n");
                    writer.write("</vehicle>\n");
                }

                writer.close();
                System.out.println("Arquivo XML esvaziado com sucesso. Dados transferidos para temp.xml");  

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erro ao esvaziar o arquivo XML.");
            }
        }

        public ArrayList<Route> getRoutes(){
            return routes;
        }
    }


    public void reconstructOriginalFile() {
        try {
            File originalFile = new File(uriRoutesXML);
            File tempFile = new File("\\data\\temp.xml");
    
            if(tempFile.exists()){
                if (tempFile.renameTo(originalFile)) {
                System.out.println("Arquivo reconstruído com sucesso.");
                    
                    // Exclui o arquivo temporário
                if (tempFile.delete()) {
                    System.out.println("Arquivo temporário excluído com sucesso.");
                } else {
                    System.out.println("Falha ao excluir o arquivo temporário.");
                }
                } else {
                    System.out.println("Falha ao reconstruir o arquivo.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao reconstruir o arquivo.");
        }
    }
    
    /*
     * Thread que implementa o servidor da empresa. 
     * cada solicitação de serviço é tratada em uma nova thread
    */
    private class CompanyServer extends Thread{

        private ServerSocket serverSocket;

        public CompanyServer() throws UnknownHostException, IOException{
            serverSocket = new ServerSocket(20181);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Aceita uma nova conexão
                    new ClientHandler(clientSocket).start(); // Inicia uma nova thread para lidar com o cliente
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                InputStream input = clientSocket.getInputStream();
                byte[] encryptedData = input.readAllBytes();

                // Descriptografa os dados
                byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(),encryptedData);

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new String(decryptedData));
                JSONObject jsonObject = (JSONObject) obj;

                if (jsonObject.get("servico").equals("REQUEST_ROUTE")) {
                    // new route handler thread
                } else if (jsonObject.get("servico").equals("SEND_INFO")) {
                    // new calculaKm thread
                }
                input.close();
                clientSocket.close();

                Thread.sleep(0, 100);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //
    private class AtribuiRota extends Thread{
        
        JSONObject jsonObject;
        public AtribuiRota(JSONObject jsonObject){
            this.jsonObject = jsonObject;
        }

        @Override
        public void run(){

        }
    }

    private class CalculaKm extends Thread{
        
        JSONObject jsonObject;
        public CalculaKm(JSONObject jsonObject){
            this.jsonObject = jsonObject;
        }

        @Override
        public void run(){
            
        }
    }

    private class BotPayment extends Thread {

        private Socket socket;
        private JSONObject jsonObject;

        public BotPayment(String idDriver) throws UnknownHostException, IOException{
            this.socket = new Socket("127.0.0.1", 20180);
            this.jsonObject = new JSONObject();
            this.jsonObject.put("driver", idDriver);
            this.jsonObject.put("servico", 3.25);
        }

        @Override
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();

                // Converte o JSON em bytes
                byte[] jsonBytes = jsonObject.toJSONString().getBytes();

                // Criptografa os dados
                byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);

                // Envia os dados criptografados para o servidor
                output.write(encryptedData);

                output.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
