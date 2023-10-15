package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * - Classe (Thread) que implementa o AlphaBank -
 * 
 * Seu funcionamento é basicamente de um servidor que controla um HashMap 
 * com todas as contas bancárias. Quando um BotPayment se conecta ao banco, é 
 * lançada uma thread responsável por lidar com aquela transferência.
 * 
 * Os BotPayments devem enviar um JSONObject criptografado na porta 20180 com os campos:
 * "idConta"{"String: id do remetente"}
 * "senha"{"String: senha do remetente - default senha=idConta"}
 * "idBeneficiario"{String: id de quem irá receber a transferência}
 * ¹"valor"{"Double: valor a ser transferido"}
 * ¹ Drivers que transferirão todo seu dinheiro não adicionam chave "valor" 
*/
public class AlphaBank implements Runnable{

    private HashMap<String, Account> contas;
    private ServerSocket serverSocket;
    
    public AlphaBank() throws UnknownHostException, IOException {
        contas = new HashMap<>();
        serverSocket = new ServerSocket(20180);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // Aceita uma nova conexão
                new Transferencia(clientSocket).start(); // Inicia uma nova thread para lidar com o cliente
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized Account getAccount(String idConta, String senha) {
        Account conta = contas.get(idConta);
        if (conta != null && conta.autenticar(senha)) {
            return conta;
        }
        return null;
    }

    public synchronized void transferePara(String idConta, double valor) {
        contas.get(idConta).recebe(valor);
    }

    private class Transferencia extends Thread{

        private String idConta, senha;
        private String idBeneficiario;
        private double valor;

        public Transferencia(Socket clientSocket) {
            try {
                InputStream input = clientSocket.getInputStream();
                byte[] encryptedData = input.readAllBytes();
                byte[] decryptedData = CryptoUtils.decrypt(CryptoUtils.getStaticKey(), CryptoUtils.getStaticIV(), encryptedData);

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new String(decryptedData));
                JSONObject dadosTranferencia = (JSONObject) obj;

                this.idConta = (String) dadosTranferencia.get("idConta");
                this.senha = (String) dadosTranferencia.get("senha");
                this.idBeneficiario = (String) dadosTranferencia.get("idBeneficiario");
                try {
                    this.valor = (double) dadosTranferencia.get("valor");
                } catch (JSONException e) {
                    this.valor = getAccount(idConta, senha).getSaldo();
                }

                input.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            getAccount(idConta, senha).debita(valor);
            transferePara(idBeneficiario, valor);
        }
    }
}
