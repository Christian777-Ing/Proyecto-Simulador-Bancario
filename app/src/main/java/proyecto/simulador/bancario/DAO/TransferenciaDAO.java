package proyecto.simulador.bancario.DAO;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Transaccion;

public class TransferenciaDAO {
    
// Registrar transacción
    public void crearTransferencia(Transaccion t) {
        String sql = """
            INSERT INTO transacciones (tipo, monto, fecha, id_cuenta_origen, id_cuenta_destino)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, t.getTipo().name());
            ps.setBigDecimal(2, t.getMonto());
            ps.setTimestamp(3, Timestamp.valueOf(t.getFecha()));
            ps.setInt(4, t.getIdCuentaOrigen());
            if (t.getIdCuentaDestino() != null) {
                ps.setInt(5, t.getIdCuentaDestino());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Listar transacciones por cuenta
    public List<Transaccion> listarPorCuenta(int idCuenta) {
        List<Transaccion> lista = new ArrayList<>();
        // Usamos LEFT JOIN para traer los números de cuenta (AC...) de origen y destino
        String sql = "SELECT t.*, " +
                    "c1.numero_cuenta AS num_origen, " +
                    "c2.numero_cuenta AS num_destino " +
                    "FROM transacciones t " +
                    "LEFT JOIN cuentas c1 ON t.id_cuenta_origen = c1.id_cuenta " +
                    "LEFT JOIN cuentas c2 ON t.id_cuenta_destino = c2.id_cuenta " +
                    "WHERE t.id_cuenta_origen = ? OR t.id_cuenta_destino = ? " +
                    "ORDER BY t.fecha DESC";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idCuenta);
            ps.setInt(2, idCuenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearTransaccion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Transaccion mapearTransaccion(ResultSet rs) throws SQLException {
        Transaccion t = new Transaccion();
        t.setIdTransaccion(rs.getInt("id_transaccion"));
        t.setTipo(Transaccion.Tipo.valueOf(rs.getString("tipo")));
        t.setMonto(rs.getBigDecimal("monto"));
        t.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        t.setIdCuentaOrigen(rs.getInt("id_cuenta_origen"));
        int idDestino = rs.getInt("id_cuenta_destino");
        if (!rs.wasNull()) {
            t.setIdCuentaDestino(idDestino);
        } else {
            t.setIdCuentaDestino(null);
        }
        // Asignar los números de cuenta legibles
        t.setNumeroCuentaOrigen(rs.getString("num_origen"));
        t.setNumeroCuentaDestino(rs.getString("num_destino"));
        return t;
    }
}