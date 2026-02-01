package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.LoginService;


public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensajeError; // Para H9: Diagnóstico de errores
    @FXML private Button btnIngresar;    // Para control de estado
    @FXML private ProgressIndicator pgCarga; // Para H1: Visibilidad de estado

    private final LoginService authService = new LoginService();

    // Método llamado al presionar el botón "Ingresar"
    @FXML
    public void onLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        // Validación básica de campos
        if (user.isEmpty() || pass.isEmpty()) {
            mostrarErrorInSitu("Por favor, ingrese usuario y contraseña.");
            return;
        }

        
        activarEstadoCarga(true);

        // Intentar autenticar al usuario
        if (authService.login(user, pass)) {
            String rol = LoginService.getUsuarioLogueado().getRol().name();
            
            // Navegación basada en roles
            String vista = rol.equals("ADMIN") ? "/View/AdminView.fxml" : "/View/CuentasView.fxml";
            navegar(vista);
        } else {
            // Fallo de autenticación
            activarEstadoCarga(false);
            mostrarErrorInSitu("Usuario o contraseña incorrectos.");
            
            // Resaltar campos erróneos
            txtUsername.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 8;");
            txtPassword.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 8;");
        }
    }

    // Muestra mensajes de error en la interfaz
    private void mostrarErrorInSitu(String mensaje) {
        if (lblMensajeError != null) {
            lblMensajeError.setText(mensaje);
            lblMensajeError.setVisible(true);
            lblMensajeError.setManaged(true);
        }
    }

    /**
     * Controla la visibilidad de los elementos de carga 
     */
    private void activarEstadoCarga(boolean cargando) {
        if (pgCarga != null) {
            pgCarga.setVisible(cargando);
            pgCarga.setManaged(cargando);
        }
        btnIngresar.setDisable(cargando); // Evita clics duplicados
        if (cargando && lblMensajeError != null) {
            lblMensajeError.setVisible(false);
        }
    }

    // Navegación entre vistas FXML 
    @FXML
    public void onIrARegistro() {
        navegar("/View/RegistroUsuarioView.fxml");
    }

    private void navegar(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Transición suave
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Solo usamos Alerta para errores críticos de sistema
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista: " + fxml, Alert.AlertType.ERROR);
            activarEstadoCarga(false);
        }
    }

    // Muestra una alerta emergente
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}