package io.sim;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class Teste extends Thread {
    private JSONObject jsonObject;
    private String operacao;

    public Teste(String operacao, int resultado) {
        this.jsonObject = new JSONObject(); // Inicializando jsonObject
        this.jsonObject.put(operacao, resultado);
        this.operacao = operacao;
    }

    @Override
    public void run() {
        try {
            byte[] jsonBytes = jsonObject.toString().getBytes();

            byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);

            byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), encryptedData);

            JSONObject jsonPackage = new JSONObject(new String(decryptedData, StandardCharsets.UTF_8));

            System.out.println(operacao + " = " + jsonPackage.getInt(operacao));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public static void main(String[] args) { 
        Teste t = new Teste("1+1", 2);
        t.start();
    }
}
