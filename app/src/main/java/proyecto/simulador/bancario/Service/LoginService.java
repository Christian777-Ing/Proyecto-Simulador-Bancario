package proyecto.simulador.bancario.Service;

import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.DAO.UsuarioDAO;


public class LoginService {
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static Usuario usuarioLogueado; // Sesión global

    public boolean login(String username, String password) {
        Usuario user = usuarioDAO.buscarPorUsername(username);
        
        // Aquí comparas el password. 
        // Si usas hash real sería: BCrypt.checkpw(password, user.getPasswordHash())
        if (user != null && user.getPasswordHash().equals(password)) {
            usuarioLogueado = user;
            return true;
        }
        return false;
    }

    public static Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }
}