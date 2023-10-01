package io.sim;

import java.util.ArrayList;

import org.json.JSONObject;

public class Driver implements Runnable{
    
    private Cliente client;
    
    private String idConta;
    private String senha;
    private Car carro;

    private Route currentRoute;
    private ArrayList<Route> toDo;
    private ArrayList<Route> done;

    @Override
    public void run() {

    }
    
    
    private class BotPayment implements Runnable{
        
        
        JSONObject jsonObject = new JSONObject();

        @Override
        public void run() {

        }
    }

}


