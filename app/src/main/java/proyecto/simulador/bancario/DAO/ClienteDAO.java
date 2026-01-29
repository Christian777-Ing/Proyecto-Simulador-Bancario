package proyecto.simulador.bancario.DAO;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Cliente;

public class ClienteDAO {

    public void crearCliente(Cliente cliente) {
        String sql = """
            INSERT INTO clientes (
                primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, 
                sexo, fecha_nacimiento, cedula, email, telefono, direccion, estado, id_usuario
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, cliente.getPrimerNombre());
            ps.setString(2, cliente.getSegundoNombre());
            ps.setString(3, cliente.getPrimerApellido());
            ps.setString(4, cliente.getSegundoApellido());
            ps.setString(5, cliente.getSexo());
            // Conversión de LocalDate a java.sql.Date para MySQL
            ps.setDate(6, Date.valueOf(cliente.getFechaNacimiento()));
            ps.setString(7, cliente.getCedula());
            ps.setString(8, cliente.getEmail());
            ps.setString(9, cliente.getTelefono());
            ps.setString(10, cliente.getDireccion());
            ps.setString(11, cliente.getEstado().name());
            ps.setInt(12, cliente.getIdUsuario());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al insertar el cliente en la base de datos.");
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
