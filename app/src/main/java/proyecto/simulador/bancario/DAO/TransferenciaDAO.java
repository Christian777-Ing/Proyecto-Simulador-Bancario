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
        String sql = "SELECT * FROM transacciones WHERE id_cuenta_origen = ? OR id_cuenta_destino = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idCuenta);
            ps.setInt(2, idCuenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaccion t = new Transaccion();
                t.setIdTransaccion(rs.getInt("id_transaccion"));
                t.setTipo(Transaccion.Tipo.valueOf(rs.getString("tipo")));
                t.setMonto(rs.getBigDecimal("monto"));
                t.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                t.setIdCuentaOrigen(rs.getInt("id_cuenta_origen"));
                int dest = rs.getInt("id_cuenta_destino");
                t.setIdCuentaDestino(rs.wasNull() ? null : dest);
                lista.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}