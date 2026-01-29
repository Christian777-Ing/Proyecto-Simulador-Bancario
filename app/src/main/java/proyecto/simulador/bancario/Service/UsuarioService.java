package proyecto.simulador.bancario.Service;

import java.util.regex.Pattern;

import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.modelo.Usuario;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final Pattern USER_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern PASS_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$");

    public int registrarUsuario(String username, String password, Usuario.Rol rol) {
        // 1. Validaciones
        validarUsuario(username, password);
        
        // 2. Crear objeto y guardar
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(password); // Idealmente usar un BCrypt aquí
        usuario.setRol(rol);
        
        return usuarioDAO.crearUsuario(usuario); // Retorna el ID generado
    }
    public int registrarNuevoUsuario(String user, String pass) {
        validarUsuario(user, pass); // Usa tus validaciones de patrones ya existentes
        Usuario u = new Usuario(0, user, pass, Usuario.Rol.CLIENTE);
        return usuarioDAO.crearUsuario(u); // Llama al DAO actualizado
    }

    public void validarUsuario(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        if (!USER_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("El usuario solo puede contener letras y números.");
        }
        if (password == null || !PASS_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial."
            );
        }
    }
}