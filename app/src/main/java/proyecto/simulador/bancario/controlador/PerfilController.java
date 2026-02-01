package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.Service.LoginService;
import proyecto.simulador.bancario.Service.UsuarioService;
import proyecto.simulador.bancario.modelo.Usuario;


public class PerfilController {

    // Elementos de Información
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;

    // Elementos de Seguridad
    @FXML private PasswordField txtClaveActual;
    @FXML private PasswordField txtClaveNueva;
    @FXML private PasswordField txtClaveConfirmar;

    // Elementos de Feedback Visual (Heurísticas de Nielsen)
    @FXML private ProgressBar progressFuerza;
    @FXML private Label lblEstadoClave;

    private Usuario userLogueado;
    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        // 1. Cargar datos de sesión
        userLogueado = LoginService.getUsuarioLogueado();

        if (userLogueado != null) {
            txtUsername.setText(userLogueado.getUsername());
            // txtEmail.setText(userLogueado.getEmail()); // Si aplica
        }

        // 2. Listener para Fuerza de Contraseña en Tiempo Real (Feedback de Norman)
        txtClaveNueva.textProperty().addListener((observable, oldValue, newValue) -> {
            actualizarIndicadorFuerza(newValue);
        });
        
        // Inicializar el estado de los indicadores
        progressFuerza.setVisible(false);
        lblEstadoClave.setVisible(false);
    }

    private void actualizarIndicadorFuerza(String password) {
        if (password == null || password.isEmpty()) {
            progressFuerza.setVisible(false);
            lblEstadoClave.setVisible(false);
            return;
        }

        progressFuerza.setVisible(true);
        lblEstadoClave.setVisible(true);

        double fuerza = calcularPuntajeFuerza(password);
        progressFuerza.setProgress(fuerza);

        // Mapeo Visual de Colores y Texto
        if (fuerza <= 0.3) {
            lblEstadoClave.setText("Fuerza: Débil ❌");
            lblEstadoClave.setStyle("-fx-text-fill: #e74c3c;"); 
            progressFuerza.setStyle("-fx-accent: #e74c3c;"); // Rojo
        } else if (fuerza <= 0.7) {
            lblEstadoClave.setText("Fuerza: Media ⚠️");
            lblEstadoClave.setStyle("-fx-text-fill: #f1c40f;");
            progressFuerza.setStyle("-fx-accent: #f1c40f;"); // Amarillo
        } else {
            lblEstadoClave.setText("Fuerza: Fuerte ✅");
            lblEstadoClave.setStyle("-fx-text-fill: #27ae60;");
            progressFuerza.setStyle("-fx-accent: #27ae60;"); // Verde
        }
    }

    private double calcularPuntajeFuerza(String pass) {
        double puntaje = 0;
        if (pass.length() >= 8) puntaje += 0.25;
        if (pass.matches(".*[A-Z].*")) puntaje += 0.25;
        if (pass.matches(".*\\d.*")) puntaje += 0.25;
        if (pass.matches(".*[@$!%*?&#].*")) puntaje += 0.25;
        return puntaje;
    }

    @FXML
    private void onGuardar() {
        if (userLogueado == null) return;

        String actual = txtClaveActual.getText();
        String nueva = txtClaveNueva.getText();
        String confirma = txtClaveConfirmar.getText();

        try {
            // Validación de coincidencia (Heurística: Prevención de errores)
            if (!nueva.equals(confirma)) {
                mostrarAlerta("Error de Validación", "Las nuevas contraseñas no coinciden.", Alert.AlertType.ERROR);
                return;
            }

            // Delegar al Service (que usa el PASS_PATTERN de 8 caracteres, Mayus, etc.)
            boolean exito = usuarioService.cambiarPassword(userLogueado.getIdUsuario(), actual, nueva);

            if (exito) {
                mostrarAlerta("Éxito", "Perfil actualizado correctamente.", Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error de Seguridad", "La contraseña actual es incorrecta.", Alert.AlertType.WARNING);
            }

        } catch (IllegalArgumentException e) {
            // Captura el mensaje de error del patrón regex en UsuarioService
            mostrarAlerta("Requisitos no cumplidos", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarAlerta("Error de Sistema", "Ocurrió un problema inesperado.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML 
    private void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String msj, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}