import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimiento {
    private int id;
    private int cuentaId;
    private String tipo; // "DEPOSITO", "RETIRO", "TRANSFERENCIA"
    private BigDecimal monto;
    private LocalDateTime fecha;
    private String descripcion;

    public Movimiento() {}

    public Movimiento(int id, int cuentaId, String tipo, BigDecimal monto, LocalDateTime fecha, String descripcion) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    // Constructor sin ID
    public Movimiento(int cuentaId, String tipo, BigDecimal monto, String descripcion) {
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = LocalDateTime.now();
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCuentaId() { return cuentaId; }
    public void setCuentaId(int cuentaId) { this.cuentaId = cuentaId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "[" + fecha + "] " + tipo + " - $" + monto + " (" + descripcion + ")";
    }
}

