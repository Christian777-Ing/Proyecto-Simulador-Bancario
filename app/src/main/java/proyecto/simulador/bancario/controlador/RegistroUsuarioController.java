package proyecto.simulador.bancario.controlador;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.UsuarioService;

public class RegistroUsuarioController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    private final UsuarioService uServ = new UsuarioService();

    @FXML
    public void onSiguiente() {
        try {
            // 1. Validar y crear el usuario en la DB
            int idGenerado = uServ.registrarNuevoUsuario(txtUser.getText(), txtPass.getText());

            // 2. Cargar la vista (Paso 2)
            // OJO: Verifica que el nombre del archivo sea exactamente este
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/RegistroClienteView.fxml"));
            Parent root = loader.load();

            // 3. Pasar el ID
            RegistroClienteController clienteCtrl = loader.getController();
            clienteCtrl.setIdUsuarioVinculado(idGenerado);

            // 4. Cambiar escena
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", "No se encontró el archivo FXML del Paso 2.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onVolver() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
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
