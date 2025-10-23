import java.math.BigDecimal;

public class Cuenta {
    private int id;
    private int usuarioId;
    private String numeroCuenta;
    private BigDecimal saldo;

    public Cuenta() {}

    public Cuenta(int id, int usuarioId, String numeroCuenta, BigDecimal saldo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    // Constructor sin ID (para crear nueva cuenta)
    public Cuenta(int usuarioId, String numeroCuenta, BigDecimal saldo) {
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    @Override
    public String toString() {
        return "Cuenta{numeroCuenta='" + numeroCuenta + "', saldo=" + saldo + "}";
    }
}
