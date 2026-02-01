package proyecto.simulador.bancario.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Usuario;


public class UsuarioDAO {

    public int crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        // ¡ESTO ES LO QUE FALTABA! Actualizar el objeto recibido
                        usuario.setIdUsuario(idGenerado); 
                        return idGenerado; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("--- ERROR AL CREAR USUARIO ---");
            if (e.getErrorCode() == 1062) { 
                throw new RuntimeException("El nombre de usuario '" + usuario.getUsername() + "' ya está ocupado.");
            }
            throw new RuntimeException("Error en la base de datos: " + e.getMessage());
        }
        return -1; 
    }
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRol(Usuario.Rol.valueOf(rs.getString("rol")));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizarPassword(int idUsuario, String passActual, String passNueva) {
        String sqlVerificar = "SELECT password_hash FROM usuarios WHERE id_usuario = ? AND password_hash = ?";
        String sqlActualizar = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";

        try (Connection conn = Conexion.getConexion()) {
            // Validación manual para evitar errores de espacios
            PreparedStatement psVerificar = conn.prepareStatement(sqlVerificar);
            psVerificar.setInt(1, idUsuario);
            psVerificar.setString(2, passActual.trim());
            
            ResultSet rs = psVerificar.executeQuery();

            if (rs.next()) {
                // Si entró aquí, la clave coincide. Procedemos a cambiarla.
                PreparedStatement psUpdate = conn.prepareStatement(sqlActualizar);
                psUpdate.setString(1, passNueva.trim());
                psUpdate.setInt(2, idUsuario);
                
                int filasAfectadas = psUpdate.executeUpdate();
                return filasAfectadas > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Retorna false si la clave actual no coincide o hubo error
    }

    public void eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al revertir registro de usuario: " + e.getMessage());
        }
    }
}

