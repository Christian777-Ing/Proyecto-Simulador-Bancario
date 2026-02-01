package proyecto.simulador.bancario.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Cuenta;

public class CuentaDAO {
    
    public void crearCuenta(Cuenta cuenta) {
        // Lógica para insertar una cuenta en la base de datos
        String sql = "INSERT INTO cuentas (numero_cuenta, tipo, saldo, estado, id_cliente) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, cuenta.getNumeroCuenta()); 
            ps.setString(2, cuenta.getTipo().name());
            ps.setBigDecimal(3, cuenta.getSaldo());
            ps.setString(4, cuenta.getEstado().name());
            ps.setInt(5, cuenta.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }   
    }

    // Buscar cuenta por ID
    public Cuenta buscarPorId(int idCuenta) {
        String sql = "SELECT * FROM cuentas WHERE id_cuenta = ?";

        try (PreparedStatement ps =Conexion.getConexion().prepareStatement(sql)) {

            ps.setInt(1, idCuenta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearCuenta(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

  // Listar cuentas por cliente
    public List<Cuenta> listarPorCliente(int idCliente) {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT * FROM cuentas WHERE id_cliente = ?";

        try (PreparedStatement ps =
                 Conexion.getConexion().prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearCuenta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void actualizarSaldo( int idCuenta, java.math.BigDecimal nuevoSaldo) {
        // Lógica para actualizar el saldo de una cuenta
        String sql = "UPDATE cuentas SET saldo = ? WHERE id_cuenta = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setInt(2, idCuenta);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cerrarCuenta(int idCuenta) {
        // Lógica para cerrar una cuenta (cambiar su estado a CERRADA)
        String sql = "UPDATE cuentas SET estado = ? WHERE id_cuenta = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, Cuenta.Estado.CERRADA.name());
            ps.setInt(2, idCuenta);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Cuenta buscarPorNumero(String numeroCuenta) {
        String sql = "SELECT * FROM cuentas WHERE numero_cuenta = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearCuenta(rs); // Reutilizamos tu lógica de mapeo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Si no existe, devolvemos null para manejar el error en el Service
    }

    public String validarTitular(String numeroCuenta, String cedula) {
        String sql = "SELECT c.primer_nombre, c.primer_apellido " +
                    "FROM cuentas cu " +
                    "JOIN clientes c ON cu.id_cliente = c.id_cliente " +
                    "WHERE cu.numero_cuenta = ? AND c.cedula = ?";
        
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            ps.setString(2, cedula);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("primer_nombre") + " " + rs.getString("primer_apellido");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No coinciden o no existe
    }

    private Cuenta mapearCuenta(ResultSet rs) throws SQLException {
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(rs.getInt("id_cuenta"));
        cuenta.setNumeroCuenta(rs.getString("numero_cuenta"));
        cuenta.setTipo(Cuenta.Tipo.valueOf(rs.getString("tipo")));
        cuenta.setSaldo(rs.getBigDecimal("saldo"));
        cuenta.setEstado(Cuenta.Estado.valueOf(rs.getString("estado")));
        cuenta.setIdCliente(rs.getInt("id_cliente"));
        return cuenta;
    }

    public String obtenerNombreTitular(String numeroCuenta, String cedula) {
        String nombreCompleto = null;
        
        // Usamos los nombres reales de tus columnas: primer_nombre y primer_apellido
        String sql = "SELECT cl.primer_nombre, cl.primer_apellido FROM clientes cl " +
                    "INNER JOIN cuentas cu ON cl.id_cliente = cu.id_cliente " +
                    "WHERE cu.numero_cuenta = ? AND cl.cedula = ?";

        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, numeroCuenta);
            ps.setString(2, cedula);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Formamos el nombre combinando las columnas reales
                    nombreCompleto = rs.getString("primer_nombre") + " " + rs.getString("primer_apellido");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en SQL obtenerNombreTitular: " + e.getMessage());
            // Es vital imprimir el error para debuguear sintaxis
            e.printStackTrace(); 
        }
        return nombreCompleto;
    }

    public boolean desactivarCuentaDB(int idCuenta) {
        String sql = "UPDATE cuentas SET estado = 'INACTIVA' WHERE id_cuenta = ?";
        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idCuenta);
            int filas = ps.executeUpdate();
            
            // Si no tienes autocommit, añade esto:
            // conn.commit(); 
            
            return filas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}