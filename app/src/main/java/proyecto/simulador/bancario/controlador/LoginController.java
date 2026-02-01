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
    private final LoginService authService = new LoginService();

    @FXML
    public void onLogin() { 
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (authService.login(user, pass)) {
            String rol = LoginService.getUsuarioLogueado().getRol().name();
            
            String vista = rol.equals("ADMIN") ? "/View/AdminView.fxml" : "/View/CuentasView.fxml";
            navegar(vista);
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos", Alert.AlertType.ERROR);
        }
    }


    @FXML
    public void onIrARegistro() {
        navegar("/View/RegistroUsuarioView.fxml");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void navegar(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { 
            e.printStackTrace(); 
            mostrarAlerta("Error de Sistema", "No se pudo cargar la vista: " + fxml, Alert.AlertType.ERROR);
        }
    }
}