package io.sim;

public class Account implements Runnable{
    
    private String idConta;
    private String senha;
    private double saldo;

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

    public void transfere(String _idConta, String _senha, double valor){
        if(autentica(_idConta, _senha)) saldo -= valor;
        else System.out.println("erro de autenticacao");
    }

    private boolean autentica(String _idConta, String _senha){
        if(_idConta.equalsIgnoreCase(idConta) && _senha.equalsIgnoreCase(senha)){
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
}
