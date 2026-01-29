package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.modelo.Cliente;

public class AdminController {
    // Campos de Usuario
    @FXML private TextField txtNewUser;
    @FXML private PasswordField txtNewPass;

    // Campos de Cliente detallados
    @FXML private TextField txtPNombre, txtSNombre, txtPApellido, txtSApellido;
    @FXML private TextField txtCedula, txtEmail, txtTelefono, txtDireccion;
    @FXML private DatePicker dpFechaNac; // Importante para la validación de edad
    @FXML private ComboBox<String> cbSexo;

    private final ClienteService clienteService = new ClienteService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void onGuardarTodo() {
        try {
            // 1. Crear el Usuario
            Usuario nuevoUsuario = new Usuario(0, txtNewUser.getText(), txtNewPass.getText(), Usuario.Rol.CLIENTE);
            usuarioDAO.crearUsuario(nuevoUsuario); 

            // 2. Crear el Cliente (El Service validará si es mayor de 18 años)
            clienteService.crearCliente(
                txtPNombre.getText(), 
                txtSNombre.getText(), 
                txtPApellido.getText(), 
                txtSApellido.getText(),
                cbSexo.getValue(), // Obtiene el sexo del ComboBox
                dpFechaNac.getValue(), // Obtiene LocalDate del DatePicker
                txtCedula.getText(), 
                txtEmail.getText(),
                txtTelefono.getText(), 
                txtDireccion.getText(), 
                Cliente.Estado.ACTIVO, 
                nuevoUsuario.getIdUsuario()
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Usuario y Cliente registrados correctamente.");
            limpiarCampos();

        } catch (IllegalArgumentException e) {
            // Aquí caerá si el cliente es menor de 18 años
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        txtNewUser.clear(); txtNewPass.clear();
        txtPNombre.clear(); txtSNombre.clear();
        txtPApellido.clear(); txtSApellido.clear();
        txtCedula.clear(); txtEmail.clear();
        dpFechaNac.setValue(null);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}