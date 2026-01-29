package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.modelo.Cliente;

public class AdminController {
    @FXML private TextField txtNombre, txtCedula, txtEmail, txtNewUser;
    @FXML private PasswordField txtNewPass;

    private final ClienteService clienteService = new ClienteService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void onGuardarTodo() {
        try {
            // 1. Crear el Usuario primero para obtener su ID
            Usuario nuevoUsuario = new Usuario(0, txtNewUser.getText(), txtNewPass.getText(), Usuario.Rol.CLIENTE);
            // Asumiendo que tu DAO actualiza el ID al insertar
            usuarioDAO.crearUsuario(nuevoUsuario); 

            // 2. Crear el Cliente vinculado a ese ID de Usuario
            clienteService.crearCliente(
                txtNombre.getText(), txtCedula.getText(), txtEmail.getText(),
                "S/N", "S/D", Cliente.Estado.ACTIVO, nuevoUsuario.getIdUsuario()
            );

            mostrarAlerta("Éxito", "Usuario y Cliente registrados correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        new Alert(Alert.AlertType.INFORMATION, contenido).show();
    }
}
