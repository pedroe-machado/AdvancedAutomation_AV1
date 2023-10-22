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
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

public class Company implements Runnable {
    public static String uriRoutesXML = "map\\map.rou.xml";

    private CompanyServer server;
    private ArrayList<Car> carrosFirma;
    private ArrayList<Driver> drivers;

    private ArrayList<Route> avaliableRoutes;
    private ArrayList<Route> runningRoutes;
    private ArrayList<Route> finishedRoutes;

    private SumoTraciConnection sumo;

    public Company(SumoTraciConnection sumo) throws Exception{
        this.sumo = sumo;
        this.carrosFirma = new ArrayList<>();
        this.server = new CompanyServer();
        this.avaliableRoutes = new ArrayList<Route>(); 
        this.runningRoutes = new ArrayList<Route>();
        this.finishedRoutes = new ArrayList<Route>();
        this.carrosFirma = new ArrayList<Car>();
        this.drivers = new ArrayList<Driver>();

        for(int i=0; i<1; i++){ // Contratação de Drivers e cadastro de novos carros            
            Car newCar = new Car(true, Integer.toString(i), new SumoColor(0, 255, 0, 126),Integer.toString(i),sumo,500,2,2,5.87,5,1);
            Driver newDriver = new Driver(sumo, Integer.toString(i), newCar);

            carrosFirma.add(i,newCar);
            drivers.add(i,newDriver);
        }
        //reconstructOriginalFile(); //testar se xml já foi limpo
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
        
        server.start();

        for (int i = 0; i <= carrosFirma.size()-1; i++) {
            carrosFirma.get(i).start();
            drivers.get(i).start();
        }

        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tarefa que obtém as routes a partir do banco de dados XML 
     * @apiNote a limpeza do arquivo XML já está implementada e deve-se reconstruir o arquivo
     * antes que o simulador seja executado novamente via reconstructOriginalFile();
     */
    private class CreateRoutes extends Thread{
        private ArrayList<Route> routes;

        @Override
        public void run(){
            this.routes = parseRoutes();
            //limpaXml();
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
                        Node node = elem.getElementsByTagName("route").item(0);
                        Element edges = (Element) node;
                        String[] edgesArray = edges.getAttribute("edges").split(" ");
                        SumoStringList edgesList = new SumoStringList();
                        for (String edge : edgesArray) {
                            edgesList.add(edge);
                        }
                        routesList.add(new Route(Integer.toString(i), edgesList)); // Adiciona corrigindo idRotas descontínuo
                    }
                }
            } catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            return routesList;
        }
        
        // private void limpaXml(){
        //     try {
        //         FileWriter fileWriter = new FileWriter(Company.uriRoutesXML);
        //         fileWriter.write("");
        //         fileWriter.close();

        //         FileWriter tempFileWriter = new FileWriter("\\data\\temp.xml");
        //         tempFileWriter.write(""); 
        //         tempFileWriter.close();
        //         BufferedWriter writer = new BufferedWriter(new FileWriter("\\data\\temp.xml"));

        //         for (Route route : avaliableRoutes) {
        //             writer.write("<vehicle id=\"" + route.getId() + "\" depart=\"0.00\">\n");
        //             writer.write("  <route edges=\"" + String.join(" ", route.getEdges()) + "\"/>\n");
        //             writer.write("</vehicle>\n");
        //         }

        //         writer.close();
        //         System.out.println("Arquivo XML esvaziado com sucesso. Dados transferidos para temp.xml");  

        //     } catch (IOException e) {
        //         e.printStackTrace();
        //         System.out.println("Erro ao esvaziar o arquivo XML.");
        //     }
        // }
        
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
    

    /**
     * Thread que implementa o servidor da empresa. 
     * cada solicitação de serviço é tratada em uma nova thread ClientHandler
     * @apiNote As conexões com o CompanyServer devem ser feitas na porta:20181
    */
    private class CompanyServer extends Service{

        public CompanyServer() throws UnknownHostException, IOException{
            super(20181);
        }

        @Override
        public Server CreateServerThread(Socket conn) {
            return new ClientHandler(conn);
        }
    }
    private class ClientHandler extends Server {

        public ClientHandler(Socket con) {
            super(con);
        }
        @Override
        protected void ProcessMessage(String jsonString){
            try {
                byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonString.getBytes());
                JSONObject jsonObject = new JSONObject((new String(decryptedData)));
                
                if (jsonObject.getString("servico").equals("REQUEST_ROUTE")) {
                    new ManageRoute(clientSocket, jsonObject);
                } else if (jsonObject.getString("servico").equals("SEND_INFO")) {
                    new CalculaKm(jsonObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Thread que gerencia as listas de rotas e retorna a rota solicitada
     * @param carSocket o qual estabelecida conexão ClientHandler-Car
     * @param jsonObject contendo rota finalizada se houver
     * 
     * @return Route a ser atribuida a um novo Service 
     */
    private class ManageRoute extends Thread{
        private JSONObject jsonRoute;
        private boolean resumedRoute;
        private Route routeFinalizada;
        private Socket carSocket;

        public ManageRoute(Socket connection, JSONObject jsonObject) {
            resumedRoute = true;
            carSocket = connection;
            jsonRoute = new JSONObject();
            try {
                routeFinalizada = (Route) jsonObject.get("routeFinished");
            } catch (Exception e) {
                resumedRoute = false;
            }
            this.start();
        }
        private synchronized void reorganize(){
            try{
                Route auxRoute = getRoutesAccess().remove(getRoutesAccess().size()-1);
                getRunningAccess().add(Integer.parseInt(auxRoute.getId()), auxRoute);
                this.jsonRoute.put("rota",auxRoute);
                if(resumedRoute){
                    getFinishedAccess().add(Integer.parseInt(routeFinalizada.getId()),routeFinalizada);
                }
            } catch (NullPointerException e){
                System.out.println("erro ao acessar lista");
            }
        }                             
        private void sendRoute(){
            try {
                OutputStream output = carSocket.getOutputStream();
                byte[] jsonBytes = jsonRoute.toString().getBytes();
                byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);
                output.write(encryptedData);
                carSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("erro ao enviar rota");
            }
        }
        @Override
        public void run(){
            reorganize();
            sendRoute();
        }
    }

    /**
     * Thread que gerencia o deslocamento de cada Car associado
     * @param jsonObject recebido pelo Company Server 
     */
    private class CalculaKm extends Thread{        
        private boolean singleMap;
        private String idAuto;
        private double distance;
        private static HashMap<String,Double> controlMap;

        public CalculaKm(JSONObject jsonObject){
            controlMap = getInstanciaMapa();
            idAuto = jsonObject.getString("idAuto");
            double newDistancia = jsonObject.getDouble("km");
            try {
                attMap(idAuto, newDistancia);
                this.start();
            } catch (NullPointerException e) {
                System.out.println("error mapa de distancias");
            }
        }
        private synchronized HashMap<String,Double> getInstanciaMapa() {
            if(!singleMap){ 
                singleMap = true;
                return new HashMap<>();
            }
            return controlMap;
        }
        private synchronized void attMap(String idAuto, double newDistance){
            distance = controlMap.get(idAuto);
            distance += newDistance;
            controlMap.put(idAuto,distance);
        }
        @Override
        public void run(){
            if(distance>=1000){
                try {
                    new BotPayment(idAuto);
                    controlMap.put(idAuto,0.0);
                } catch (UnknownHostException e) {
                    System.out.println("erro ao tentar pagar motorista" + idAuto + " banco offline");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }       
        }
    }

    /**
     * Thread BotPayment que realiza um pagamento ao driver que completou 1km
     * @param idDriver que irá receber a transferência
     */
    private class BotPayment extends Thread {
        private Socket socket;
        private JSONObject jsonObject;

        public BotPayment(String idDriver) throws UnknownHostException, IOException{
            this.socket = new Socket("127.0.0.1", 20180);
            this.jsonObject = new JSONObject();
            this.jsonObject.put("idConta", "company");
            this.jsonObject.put("senha", "company");
            this.jsonObject.put("idBeneficiario", idDriver);
            this.jsonObject.put("valor", 3.25);
            this.start();
        }
        @Override
        public void run() {
            try {
                OutputStream output = socket.getOutputStream();
                // Converte o JSON em bytes
                byte[] jsonBytes = jsonObject.toString().getBytes();
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
      
    public ArrayList<String> getCLTs(){
        ArrayList<String> listaCLT = new ArrayList<>();
        for (Driver clt: drivers) {
            listaCLT.add(clt.getIdDriver());
        }
        return listaCLT;
    }
    
    private synchronized ArrayList<Route> getRunningAccess(){
        return runningRoutes;
    }

    private synchronized ArrayList<Route> getFinishedAccess(){
        return finishedRoutes;
    }
    
    private synchronized ArrayList<Route> getRoutesAccess(){
        return avaliableRoutes;
    }

    public SumoTraciConnection getSumo() {
        return sumo;
    }
}
