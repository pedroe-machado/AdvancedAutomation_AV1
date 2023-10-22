package io.sim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
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

    private HashMap<String, Account> accounts;
    private ServerSocket serverSocket;
    
    public AlphaBank(ArrayList<String> users) throws UnknownHostException, IOException {
        accounts = new HashMap<>();
        for (String id : users) {
            accounts.put(id, new Account(id, 0));
        }
        serverSocket = new ServerSocket(20180);
        new Thread(this).start();
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
        Account conta = accounts.get(idConta);
        if (conta != null && conta.autenticar(senha)) {
            return conta;
        }
        return null;
    }

    public synchronized void transferePara(String idConta, double valor) {
        accounts.get(idConta).recebe(valor);
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

                JSONObject dadosTranferencia = new JSONObject((new String(decryptedData)));

                input.close();

                this.idConta = (String) dadosTranferencia.get("idConta");
                this.senha = (String) dadosTranferencia.get("senha");
                this.idBeneficiario = (String) dadosTranferencia.get("idBeneficiario");
                try {
                    this.valor = (double) dadosTranferencia.get("valor");
                } catch (JSONException e) {
                    this.valor = getAccount(idConta, senha).getSaldo();   
                }

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

    private class Account{    
        private String idConta;
        private String senha;
        private double saldo;
        private boolean autenticado;
    
        public Account(String idConta, double saldo){
            this.idConta = this.senha = idConta;
            this.saldo = saldo;
        }
    
        public synchronized double getSaldo(){
            return saldo;
        }
    
        public synchronized void recebe(double valor){
            saldo += valor;
        }
    
        public synchronized void debita(double valor){
            if(autenticado) saldo -= valor;
            else System.out.println("erro de autenticacao");
        }
    
        public synchronized boolean autenticar(String _senha){
            if( _senha.equalsIgnoreCase(senha)){
                autenticado = true;
                return true;
            }
            return false;
        }
    }
}
