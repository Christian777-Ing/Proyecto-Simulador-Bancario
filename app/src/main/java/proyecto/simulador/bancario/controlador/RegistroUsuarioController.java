package proyecto.simulador.bancario.controlador;

import java.io.IOException;




import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.UsuarioService;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegistroUsuarioController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    private final UsuarioService uServ = new UsuarioService();

    @FXML
    public void onSiguiente() {
        try {
            // Usamos el ClassLoader para mayor compatibilidad con Gradle
            URL fxmlLocation = getClass().getResource("/View/RegistroClienteView.fxml");
            
            if (fxmlLocation == null) {
                // Intento alternativo sin el slash inicial
                fxmlLocation = getClass().getClassLoader().getResource("View/RegistroClienteView.fxml");
            }

            if (fxmlLocation == null) {
                throw new Exception("Error: No se encontró el archivo FXML. Verifique la ruta.");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // PASO CRÍTICO: Registramos al usuario SOLO si la vista cargó correctamente
            int idGenerado = uServ.registrarNuevoUsuario(txtUser.getText(), txtPass.getText());

            RegistroClienteController clienteCtrl = loader.getController();
            clienteCtrl.setIdUsuarioVinculado(idGenerado);

            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", e.getMessage());
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

