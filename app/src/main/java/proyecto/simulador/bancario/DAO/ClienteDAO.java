package proyecto.simulador.bancario.DAO;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Cliente;

public class ClienteDAO {

    public void crearCliente(Cliente cliente) {
        String sql = """
            INSERT INTO clientes (nombre, cedula, email, telefono, direccion, estado, id_usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getCedula());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getDireccion());
            ps.setString(6, cliente.getEstado().name());
            ps.setInt(7, cliente.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarEstado(int idCliente, Cliente.Estado estado) {
        String sql = "UPDATE clientes SET estado = ? WHERE id_cliente = ?";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
