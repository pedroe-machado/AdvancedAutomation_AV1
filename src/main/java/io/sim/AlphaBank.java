package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AlphaBank implements Runnable{

    private ArrayList<Account> contas;
    private Servidor server;

    public AlphaBank() throws UnknownHostException, IOException{
        this.server = new Servidor(new Socket("127.0.0.1", 15000),CryptoUtils.getStaticKey(),CryptoUtils.getStaticIV());
    }

    @Override
    public void run() {

    }

    
}
