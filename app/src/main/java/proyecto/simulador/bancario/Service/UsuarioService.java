package proyecto.simulador.bancario.Service;

import java.util.regex.Pattern;

public class UsuarioService {

    // Pre-compilar los patrones mejora el rendimiento
    private static final Pattern USER_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern PASS_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$");

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