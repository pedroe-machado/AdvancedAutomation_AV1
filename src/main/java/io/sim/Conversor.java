package io.sim;

public class Conversor extends Thread{
    private double longitude;
    private double latitude;
    private double cartesianoX;
    private double cartesianoY;
    private double[] historicoLatitude;
    private double[] historicoLongitude;
    private int aux;
    private double distancia;

    public Conversor(){
        historicoLatitude = new double[2];
        historicoLongitude = new double[2];
        aux = 0;
        distancia = 0;
    }

    public double converter(String posicao) throws Exception{
        String[] partes = posicao.split(",");
        try {
            cartesianoX = Double.parseDouble(partes[0]);
            cartesianoY = Double.parseDouble(partes[1]);
        } catch (NumberFormatException e) {}
        
        double latitudeOrigem = 40.0;  // Latitude da origem em graus
        double longitudeOrigem = -75.0;  // Longitude da origem em graus
        
        // Constante para conversão de graus para radianos
        double degToRad = Math.PI / 180.0;
        
        // Converter coordenadas cartesianas para latitude e longitude
        latitude = latitudeOrigem + (cartesianoY / 111320.0);
        longitude = longitudeOrigem + (cartesianoX / (111320.0 * Math.cos(latitudeOrigem * degToRad)));
        preencheVetor();
        return distancia;
    }

    private void preencheVetor(){
        if(aux == 1){
            historicoLatitude[1] = latitude;
            historicoLongitude[1] = longitude;
            distanciaPercorrida();
        } else{
            historicoLatitude[0] = latitude;
            historicoLongitude[0] = longitude;
            aux++;
        }
    }
    
    public void distanciaPercorrida(){
        double R = 6371;

        // Diferença de latitude e longitude
        double dlat = Math.toRadians(historicoLatitude[1]) - Math.toRadians(historicoLatitude[0]);
        double dlon = Math.toRadians(historicoLongitude[1]) - Math.toRadians(historicoLongitude[0]);

        // Fórmula de Haversine
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                   Math.cos(Math.toRadians(historicoLatitude[0])) * Math.cos(Math.toRadians(historicoLatitude[1])) *
                   Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distância em metros
        distancia = R * c * 1000;
        historicoLatitude[0] = historicoLatitude[1];
        historicoLongitude[0] = historicoLongitude[1];

    }

}
