package proyecto.simulador.bancario.controlador;

import java.io.IOException;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.Service.UsuarioService;

public class RegistroController {

    @FXML private TextField txtUser, txtPass; // Nota: Usar PasswordField en FXML
    @FXML private TextField txtPNombre, txtSNombre, txtPApellido, txtSApellido;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtCedula, txtEmail, txtTelf, txtDir;
    @FXML private ComboBox<String> cbSexo;

    private final UsuarioDAO uDao = new UsuarioDAO();
    private final UsuarioService uServ = new UsuarioService();
    private final ClienteService cServ = new ClienteService();

    @FXML
    public void initialize() {
        cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        
        
        LocalDate hace18Anios = LocalDate.now().minusYears(18);
        LocalDate hace130Anios = LocalDate.now().minusYears(130);

         // 1. Sugerir una fecha inicial válida al abrir el calendario
        dpFecha.setValue(hace18Anios);

        // 2. Aplicar la restricción visual
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Bloquea si es después de "hace 18 años" o antes de "hace 130"
                setDisable(empty || date.isAfter(hace18Anios) || date.isBefore(hace130Anios));
            }
        });
    }

    @FXML
    public void onRegistrar() {
        try {
            // --- PASO 1: VALIDACIONES DE FORMATO Y LÓGICA (Memoria) ---
            // Validamos usuario y contraseña (sin insertar aún)
            uServ.validarUsuario(txtUser.getText(), txtPass.getText());

            // Validaciones de la interfaz
            if (dpFecha.getValue() == null) throw new IllegalArgumentException("Fecha de nacimiento obligatoria.");
            if (cbSexo.getValue() == null) throw new IllegalArgumentException("Debe seleccionar un sexo.");
            
            // Validamos los datos del cliente antes de llamar al DAO
            // (Podemos crear un método de validación en ClienteService que no inserte)
            validarDatosClienteLocales(); 

            // --- PASO 2: PERSISTENCIA (Base de Datos) ---
            // Solo llegamos aquí si TODO lo anterior fue correcto.
            
            // 1. Intentamos crear el usuario
            int idGenerado = uServ.registrarNuevoUsuario(txtUser.getText(), txtPass.getText());

            // 2. Intentamos crear el cliente
            cServ.crearCliente(
                txtPNombre.getText(), txtSNombre.getText(), txtPApellido.getText(), txtSApellido.getText(),
                cbSexo.getValue(), dpFecha.getValue(), txtCedula.getText(), txtEmail.getText(),
                txtTelf.getText(), txtDir.getText(), Cliente.Estado.ACTIVO, idGenerado
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Registro completado con éxito.");
            onVolver();

        } catch (IllegalArgumentException e) {
            // Si algo falla aquí, NUNCA se ejecutó uServ.registrarNuevoUsuario
            mostrarAlerta(Alert.AlertType.WARNING, "Dato Incorrecto", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Método auxiliar para validar campos de texto antes de ir a la DB
    private void validarDatosClienteLocales() {
        if (txtPNombre.getText().isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (txtCedula.getText().length() != 10) throw new IllegalArgumentException("La cédula debe tener 10 dígitos.");
        // Agrega aquí todas las validaciones de texto que necesites
    }
    @FXML
    public void onVolver() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}