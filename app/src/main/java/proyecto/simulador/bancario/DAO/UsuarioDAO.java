package proyecto.simulador.bancario.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Usuario;

public class UsuarioDAO {

    public void crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?)";

        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    // Mapeo del Enum
                    user.setRol(Usuario.Rol.valueOf(rs.getString("rol")));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

