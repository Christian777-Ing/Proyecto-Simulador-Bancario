package proyecto.simulador.bancario.Service;

import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.DAO.UsuarioDAO;


public class LoginService {
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static Usuario usuarioLogueado; 

    public boolean login(String username, String password) {
        Usuario user = usuarioDAO.buscarPorUsername(username);
        
        // Verificación básica de credenciales
        if (user != null && user.getPasswordHash().equals(password)) {
            usuarioLogueado = user;
            return true;
        }
        return false;
    }

    public static Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    // Crucial para que el botón 'Cerrar Sesión' del CuentaController funcione
    public static void setUsuarioLogueado(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    // Método de conveniencia para verificar roles rápidamente
    public static boolean esAdmin() {
        return usuarioLogueado != null && "ADMIN".equals(usuarioLogueado.getRol());
    }
}