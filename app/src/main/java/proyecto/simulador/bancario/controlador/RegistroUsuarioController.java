package proyecto.simulador.bancario.controlador;

import java.io.IOException;




import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.UsuarioService;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class RegistroUsuarioController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private ProgressBar progressFuerza;
    @FXML private Label lblEstadoClave;
    @FXML private Button btnSiguiente; // Importante para habilitar/deshabilitar

    private final UsuarioService uServ = new UsuarioService();

    @FXML
    public void initialize() {
        // Inicializar indicadores ocultos
        progressFuerza.setVisible(false);
        lblEstadoClave.setVisible(false);
        btnSiguiente.setDisable(true); // Deshabilitar hasta que la clave sea válida

        // Listener para la contraseña (Principio de Norman: Feedback)
        txtPass.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarIndicadorFuerza(newVal);
        });
    }

    private void actualizarIndicadorFuerza(String password) {
        if (password == null || password.isEmpty()) {
            progressFuerza.setVisible(false);
            lblEstadoClave.setVisible(false);
            btnSiguiente.setDisable(true);
            return;
        }

        progressFuerza.setVisible(true);
        lblEstadoClave.setVisible(true);

        double fuerza = calcularPuntaje(password);
        progressFuerza.setProgress(fuerza);

        // Heurística de Nielsen: Visibilidad del estado del sistema
        if (fuerza <= 0.3) {
            lblEstadoClave.setText("Muy débil ❌");
            progressFuerza.setStyle("-fx-accent: #e74c3c;"); // Rojo
            btnSiguiente.setDisable(true);
        } else if (fuerza <= 0.7) {
            lblEstadoClave.setText("Fuerza media ⚠️");
            progressFuerza.setStyle("-fx-accent: #f1c40f;"); // Amarillo
            btnSiguiente.setDisable(true);
        } else {
            lblEstadoClave.setText("¡Contraseña segura! ✅");
            progressFuerza.setStyle("-fx-accent: #27ae60;"); // Verde
            btnSiguiente.setDisable(false); // SOLO AQUÍ habilitamos el botón
        }
    }

    private double calcularPuntaje(String pass) {
        double p = 0;
        if (pass.length() >= 8) p += 0.25;
        if (pass.matches(".*[A-Z].*")) p += 0.25;
        if (pass.matches(".*\\d.*")) p += 0.25;
        if (pass.matches(".*[@$!%*?&#].*")) p += 0.25;
        return p;
    }

    @FXML
    public void onSiguiente() {
        try {
            // Cargar el FXML de RegistroClienteView.fxml
            URL fxmlLocation = getClass().getResource("/View/RegistroClienteView.fxml");
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getClassLoader().getResource("View/RegistroClienteView.fxml");
            }

            if (fxmlLocation == null) throw new Exception("Error: No se encontró el FXML.");

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Registrar el usuario y obtener el ID generado
            int idGenerado = uServ.registrarNuevoUsuario(txtUser.getText(), txtPass.getText());

            RegistroClienteController clienteCtrl = loader.getController();
            clienteCtrl.setIdUsuarioVinculado(idGenerado);

            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Registro", e.getMessage());
        }
    }

    @FXML
    public void onVolver() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/View/LoginView.fxml")
            );
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

