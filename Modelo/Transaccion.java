import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaccion {

    private int idTransaccion;
    private Tipo tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private int idCuentaOrigen;
    private Integer idCuentaDestino;

    public enum Tipo {
        DEPOSITO, RETIRO, TRANSFERENCIA
    }

    public Transaccion() {}

    public Transaccion(int idTransaccion, Tipo tipo, BigDecimal monto,
                       LocalDateTime fecha, int idCuentaOrigen,
                       Integer idCuentaDestino) {
        this.idTransaccion = idTransaccion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.idCuentaOrigen = idCuentaOrigen;
        this.idCuentaDestino = idCuentaDestino;
    }

    // Getters y Setters
    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdCuentaOrigen() {
        return idCuentaOrigen;
    }

    public void setIdCuentaOrigen(int idCuentaOrigen) {
        this.idCuentaOrigen = idCuentaOrigen;
    }

    public Integer getIdCuentaDestino() {
        return idCuentaDestino;
    }

    public void setIdCuentaDestino(Integer idCuentaDestino) {
        this.idCuentaDestino = idCuentaDestino;
    }
}

