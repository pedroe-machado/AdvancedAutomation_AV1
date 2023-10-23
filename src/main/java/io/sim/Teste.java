package io.sim;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.Test;

public class Teste {

    @Test
    void testOperacao() {
        String operacao = "1+1";
        int resultado = 2;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(operacao, resultado);

        try {
            byte[] jsonBytes = jsonObject.toString().getBytes("UTF-8");

            byte[] encryptedData = CryptoUtils.encrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), jsonBytes);

            byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), encryptedData);

            JSONObject jsonPackage = new JSONObject(new String(decryptedData, StandardCharsets.UTF_8));

            int resultadoObtido = jsonPackage.getInt(operacao);

            assertEquals(resultado, resultadoObtido);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
