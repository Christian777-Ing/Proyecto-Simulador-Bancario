package proyecto.simulador.bancario.controlador;

import java.time.LocalDate;
import java.time.Period;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.Service.UsuarioService;
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
    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void onGuardarTodo() {
        try {
            // 1. VALIDACIÓN DE CAMPOS VACÍOS (Básica)
            validarCamposNoNulos();

            // 2. VALIDACIÓN DE USUARIO (RegEx de letras/números y contraseña fuerte)
            // Esto usa tu UsuarioService
            usuarioService.validarUsuario(txtNewUser.getText(), txtNewPass.getText());

            // 3. VALIDACIÓN DE FECHA (18 - 130 años)
            // El método crearCliente del Service ya dispara estas excepciones
            LocalDate fechaNac = dpFechaNac.getValue();

            // 4. PROCESO DE GUARDADO
            // Primero el Usuario para obtener el ID generado
            Usuario nuevoUsuario = new Usuario(0, txtNewUser.getText(), txtNewPass.getText(), Usuario.Rol.CLIENTE);
            usuarioDAO.crearUsuario(nuevoUsuario); 

            // Luego el Cliente vinculado
            clienteService.crearCliente(
                txtPNombre.getText(), 
                txtSNombre.getText(), 
                txtPApellido.getText(), 
                txtSApellido.getText(),
                cbSexo.getValue(), 
                fechaNac, 
                txtCedula.getText(), 
                txtEmail.getText(),
                txtTelefono.getText(), 
                txtDireccion.getText(), 
                Cliente.Estado.ACTIVO, 
                nuevoUsuario.getIdUsuario()
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Administrador: El nuevo cliente ha sido registrado correctamente.");
            limpiarCampos();

        } catch (IllegalArgumentException e) {
            // Aquí caerán todos los errores de validación (edad, formato de usuario, pass débil, campos vacíos)
            mostrarAlerta(Alert.AlertType.WARNING, "Error de Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "No se pudo completar el registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validarCamposNoNulos() {
        if (txtNewUser.getText().isEmpty() || txtNewPass.getText().isEmpty() || 
            txtPNombre.getText().isEmpty() || txtPApellido.getText().isEmpty() || 
            txtCedula.getText().isEmpty() || dpFechaNac.getValue() == null) {
            throw new IllegalArgumentException("Todos los campos marcados como obligatorios deben estar llenos.");
        }
    }

   private void limpiarCampos() {
        txtNewUser.clear(); txtNewPass.clear();
        txtPNombre.clear(); txtSNombre.clear();
        txtPApellido.clear(); txtSApellido.clear();
        txtCedula.clear(); txtEmail.clear();
        txtTelefono.clear(); txtDireccion.clear();
        dpFechaNac.setValue(null);
        cbSexo.setValue(null);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}