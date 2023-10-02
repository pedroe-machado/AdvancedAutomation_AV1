package io.sim;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.*;
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
    public Servidor serverCar;

    public Company(){
        reconstructOriginalFile();
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
    class CreateRoutes extends Thread{
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao reconstruir o arquivo.");
        }
    }
    
    // Conecta carros

    //

    // Pagamentos 
    public void pagaMotorista(String idDriver, double valor) throws UnknownHostException, IOException{
        
        Socket socketBanco = new Socket("127.0.0.1", 15000);
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("driver", idDriver);
        jsonObject.put("valor", valor);

        new BotPayment(socketBanco, jsonObject);
    }

    private class BotPayment extends Cliente implements Runnable{

        public BotPayment(Socket socketBanco, JSONObject jsonObject){
            super(socketBanco,CryptoUtils.getStaticKey(),CryptoUtils.getStaticIV(), jsonObject);
            new Thread(this).start();
        }

        @Override
        public void run() {
            super.run();
        }
    }
    
}
