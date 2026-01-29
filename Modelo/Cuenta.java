package modelo;

import java.math.BigDecimal;

public class Cuenta {

    private int idCuenta;
    private String numeroCuenta;
    private Tipo tipo;
    private BigDecimal saldo;
    private Estado estado;
    private int idCliente;

    public enum Tipo {
        AHORROS, CORRIENTE
    }

    public enum Estado {
        ACTIVA, CERRADA
    }

    public Cuenta() {}

    public Cuenta(int idCuenta, String numeroCuenta, Tipo tipo,
                  BigDecimal saldo, Estado estado, int idCliente) {
        this.idCuenta = idCuenta;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.saldo = saldo;
        this.estado = estado;
        this.idCliente = idCliente;
    }

    // Getters y Setters
    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }
}

