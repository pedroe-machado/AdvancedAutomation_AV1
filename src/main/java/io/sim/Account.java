package io.sim;

public class Account{
    
    private String idConta;
    private String senha;
    private double saldo;
    private boolean autenticado;

    public Account(String idConta, double saldo){
        this.idConta = this.senha = idConta;
        this.saldo = saldo;
    }

    public double getSaldo(){
        return saldo;
    }
    
    public String getIdConta(){
        return idConta;
    }

    public void recebe(double valor){
        saldo += valor;
    }

    public void debita(double valor){
        if(autenticado) saldo -= valor;
        else System.out.println("erro de autenticacao");
    }

    public boolean autenticar(String _senha){
        if( _senha.equalsIgnoreCase(senha)){
            autenticado = true;
            return true;
        }
        return false;
    }
}
