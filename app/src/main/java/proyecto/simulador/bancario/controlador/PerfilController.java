package proyecto.simulador.bancario.controlador;


import javafx.scene.text.Text;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import proyecto.simulador.bancario.DAO.AdminDAO;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.Service.LoginService;
import proyecto.simulador.bancario.Service.UsuarioService;
import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.modelo.Usuario;

public class PerfilController {

    // --- Datos de Usuario (Cuenta) ---
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private Text txtFechaRegistro;

    // --- Datos de Cliente (Detalles Personales) ---
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblCedula;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtDireccion;

    // --- Seguridad ---
    @FXML private VBox seccionSeguridad;
    @FXML private PasswordField txtClaveActual;
    @FXML private PasswordField txtClaveNueva;
    @FXML private PasswordField txtClaveConfirmar;

    // --- Feedback ---
    @FXML private ProgressBar progressFuerza;
    @FXML private Label lblEstadoClave;
    @FXML private Button btnMostrarCambioClave;

    private Usuario userLogueado;
    private final UsuarioService usuarioService = new UsuarioService();
    private final AdminDAO adminDAO = new AdminDAO();

    @FXML
    public void initialize() {
        // Obtener usuario de la sesión actual
        userLogueado = LoginService.getUsuarioLogueado();

        if (userLogueado != null) {
            // 1. Llenar datos básicos del Usuario
            txtUsername.setText(userLogueado.getUsername());
            
            // 2. Cargar detalles extendidos de la tabla Cliente
            cargarDatosPersonales(userLogueado.getIdUsuario());
        }

        // Listener para la validación de fuerza de contraseña
        txtClaveNueva.textProperty().addListener((obs, old, val) -> actualizarIndicadorFuerza(val));
        
        // Aseguramos que la sección inicie oculta si no se configuró en el FXML
        if (seccionSeguridad != null) {
            seccionSeguridad.setVisible(false);
            seccionSeguridad.setManaged(false);
        }
    }

    private void cargarDatosPersonales(int idUsuario) {
        try {
            Cliente cliente = adminDAO.obtenerDetallesCliente(idUsuario);
            if (cliente != null) {
                // Combinar nombres y apellidos
                String nombreCompleto = String.format("%s %s %s", 
                    cliente.getPrimerNombre(), 
                    cliente.getPrimerApellido(), 
                    cliente.getSegundoApellido() != null ? cliente.getSegundoApellido() : "").trim();

                if (lblNombreCompleto != null) lblNombreCompleto.setText(nombreCompleto);
                if (lblCedula != null) lblCedula.setText("C.I.: " + cliente.getCedula());
                if (txtTelefono != null) txtTelefono.setText(cliente.getTelefono());
                if (txtDireccion != null) txtDireccion.setText(cliente.getDireccion());
                
                // Si el email del cliente es el principal, usarlo
                if (txtEmail != null) txtEmail.setText(cliente.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos del cliente: " + e.getMessage());
        }
    }

    @FXML
    private void toggleSeccionSeguridad() {
        if (seccionSeguridad == null) return;

        boolean estaVisible = seccionSeguridad.isVisible();
        seccionSeguridad.setVisible(!estaVisible);
        seccionSeguridad.setManaged(!estaVisible);
        
        btnMostrarCambioClave.setText(!estaVisible ? "🔼 Ocultar Cambio de Contraseña" : "🔐 Cambiar Contraseña");
        
        if (!estaVisible) {
            txtClaveActual.requestFocus();
        } else {
            limpiarCamposPassword();
        }
    }

    @FXML
    private void onGuardar() {
        if (userLogueado == null) return;

        try {
            // 1. Guardar cambios en datos de contacto (Opcional si permites edición)
            // adminDAO.actualizarContactoCliente(userLogueado.getIdUsuario(), txtTelefono.getText(), txtDireccion.getText());

            // 2. Lógica de cambio de contraseña
            if (seccionSeguridad.isVisible()) {
                String actual = txtClaveActual.getText();
                String nueva = txtClaveNueva.getText();
                String confirma = txtClaveConfirmar.getText();

                // Validaciones básicas
                if (actual.isEmpty() || nueva.isEmpty() || confirma.isEmpty()) {
                    mostrarAlerta("Campos Requeridos", "Por favor, llene todos los campos de seguridad.", Alert.AlertType.WARNING);
                    return;
                }

                if (!nueva.equals(confirma)) {
                    mostrarAlerta("Error", "La nueva contraseña y la confirmación no coinciden.", Alert.AlertType.ERROR);
                    return;
                }

                boolean exito = usuarioService.cambiarPassword(userLogueado.getIdUsuario(), actual, nueva);
                if (exito) {
                    mostrarAlerta("Éxito", "Contraseña actualizada correctamente.", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "La contraseña actual es incorrecta.", Alert.AlertType.ERROR);
                    return;
                }
            }
            // 3. Confirmación de guardado exitoso
            mostrarAlerta("Perfil Actualizado", "Los datos se han guardado con éxito.", Alert.AlertType.INFORMATION);
            cerrarVentana();

        } catch (Exception e) { // Captura errores inesperados
            mostrarAlerta("Error de Sistema", "No se pudieron guardar los cambios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Actualiza el indicador de fuerza de la contraseña en tiempo real
    private void actualizarIndicadorFuerza(String password) {
        if (password == null || password.isEmpty()) {
            if (progressFuerza != null) progressFuerza.setVisible(false);
            if (lblEstadoClave != null) lblEstadoClave.setVisible(false);
            return;
        }
        // Mostrar indicadores
        progressFuerza.setVisible(true);
        lblEstadoClave.setVisible(true);
        
        double fuerza = calcularPuntajeFuerza(password);
        progressFuerza.setProgress(fuerza);

        // Actualizar texto y color según la fuerza
        if (fuerza <= 0.3) {
            lblEstadoClave.setText("Fuerza: Débil ❌");
            lblEstadoClave.setStyle("-fx-text-fill: #e74c3c;");
            progressFuerza.setStyle("-fx-accent: #e74c3c;");
        } else if (fuerza <= 0.7) {
            lblEstadoClave.setText("Fuerza: Media ⚠️");
            lblEstadoClave.setStyle("-fx-text-fill: #f1c40f;");
            progressFuerza.setStyle("-fx-accent: #f1c40f;");
        } else {
            lblEstadoClave.setText("Fuerza: Alta ✅");
            lblEstadoClave.setStyle("-fx-text-fill: #27ae60;");
            progressFuerza.setStyle("-fx-accent: #27ae60;");
        }
    }

    // Calcula un puntaje simple de fuerza de contraseña
    private double calcularPuntajeFuerza(String pass) {
        double puntaje = 0;
        if (pass.length() >= 8) puntaje += 0.25;
        if (pass.matches(".*[A-Z].*")) puntaje += 0.25;
        if (pass.matches(".*\\d.*")) puntaje += 0.25;
        if (pass.matches(".*[@$!%*?&#].*")) puntaje += 0.25;
        return puntaje;
    }

    @FXML private void onCancelar() { cerrarVentana(); }

    // Cierra la ventana actual
    private void cerrarVentana() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    //  Limpia los campos de cambio de contraseña
    private void limpiarCamposPassword() {
        txtClaveActual.clear();
        txtClaveNueva.clear();
        txtClaveConfirmar.clear();
        if (progressFuerza != null) progressFuerza.setProgress(0);
    }

    private void mostrarAlerta(String titulo, String msj, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}