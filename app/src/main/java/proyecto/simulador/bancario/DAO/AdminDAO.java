package proyecto.simulador.bancario.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.modelo.Usuario;


public class AdminDAO {

    // Obtener el patrimonio total del banco sumando los saldos de todas las cuentas
    public BigDecimal obtenerPatrimonioTotal() {
        String sql = "SELECT SUM(saldo) FROM cuentas";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    // Listar usuarios respetando tu clase original
    public List<Usuario> listarTodosLosUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, username, rol FROM usuarios";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("username"));
                u.setRol(Usuario.Rol.valueOf(rs.getString("rol")));
                lista.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // Obtener un mapa de estados de clientes por su ID de usuario
    public Map<Integer, String> obtenerEstadosClientes() {
        Map<Integer, String> estados = new HashMap<>();
        String sql = "SELECT id_usuario, estado FROM clientes";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                estados.put(rs.getInt("id_usuario"), rs.getString("estado"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return estados;
    }

    // Cambiar el estado de un cliente (ACTIVO/INACTIVO) por su ID de usuario
    public boolean cambiarEstadoCliente(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE clientes SET estado = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.toUpperCase());
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener detalles completos del cliente por su ID de usuario
    public Cliente obtenerDetallesCliente(int idUsuario) {
        String sql = "SELECT * FROM clientes WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setPrimerNombre(rs.getString("primer_nombre"));
                    cliente.setPrimerApellido(rs.getString("primer_apellido"));
                    cliente.setSegundoNombre(rs.getString("segundo_nombre"));
                    cliente.setSegundoApellido(rs.getString("segundo_apellido"));
                    cliente.setCedula(rs.getString("cedula"));
                    cliente.setSexo(rs.getString("sexo"));
                    cliente.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDireccion(rs.getString("direccion"));
                    cliente.setEstado(Cliente.Estado.valueOf(rs.getString("estado")));
                    return cliente;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Listar todas las cuentas asociadas a un ID de usuario
    public List<Cuenta> listarCuentasPorUsuario(int idUsuario) {
        List<Cuenta> cuentas = new ArrayList<>();
        // Consulta que une el id_usuario con sus cuentas pasando por la tabla clientes
        String sql = "SELECT numero_cuenta, tipo, saldo " +
                    "FROM cuentas " +
                    "WHERE id_cliente = (SELECT id_cliente FROM clientes WHERE id_usuario = ?)";

        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 1. Creamos la instancia del objeto
                    Cuenta cuenta = new Cuenta();
                    
                    // 2. MAPEAMOS cada columna de la DB al atributo del objeto Java
                    cuenta.setNumeroCuenta(rs.getString("numero_cuenta"));
                    cuenta.setSaldo(rs.getBigDecimal("saldo"));
                    
                    // 3. MAPEAMOS el Enum (Asegúrate que en la DB diga 'AHORRO' o 'CORRIENTE')
                    try {
                        String tipoString = rs.getString("tipo").toUpperCase().trim();
                        cuenta.setTipo(Cuenta.Tipo.valueOf(tipoString));
                    } catch (Exception e) {
                        System.err.println("Error mapeando tipo_cuenta: " + e.getMessage());
                    }

                    // 4. Agregamos el objeto ya lleno a la lista
                    cuentas.add(cuenta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en listarCuentasPorUsuario: " + e.getMessage());
        }
        return cuentas;
    }

    // Obtener el estado del cliente (ACTIVO/INACTIVO) por su ID de usuario
    public String obtenerEstadoClientePorUsuario(int idUsuario) {
        Cliente cliente = obtenerDetallesCliente(idUsuario);
        if (cliente == null) {
            return "Desconocido";
        }

        try {
            // Try getEstado()
            java.lang.reflect.Method m = Cliente.class.getMethod("getEstado");
            Object val = m.invoke(cliente);
            if (val == null) return "Desconocido";
            if (val instanceof Boolean) return ((Boolean) val) ? "Activo" : "Inactivo";
            return val.toString();
        } catch (NoSuchMethodException e) {
            // Try isActivo()
            try {
                java.lang.reflect.Method m = Cliente.class.getMethod("isActivo");
                Object val = m.invoke(cliente);
                if (val instanceof Boolean) return ((Boolean) val) ? "Activo" : "Inactivo";
            } catch (Exception ignored) { }
        } catch (Exception ignored) { }

        return "Desconocido";
    }
}
