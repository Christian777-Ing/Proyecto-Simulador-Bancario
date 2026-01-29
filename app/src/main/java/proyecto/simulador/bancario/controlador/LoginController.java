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
        if (authService.login(txtUsername.getText(), txtPassword.getText())) {
            String vista = (LoginService.getUsuarioLogueado().getRol().name().equals("ADMIN"))
                            ? "/View/AdminView.fxml" : "/View/CuentasView.fxml";
            navegar(vista);
        } else {
            new Alert(Alert.AlertType.ERROR, "Credenciales inválidas").show();
        }
    }

    // AHORA REDIRIGE AL PASO 1 (USUARIO)
    @FXML 
    public void onIrARegistro() { 
        navegar("/View/RegistroUsuarioView.fxml"); 
    }

    private void navegar(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { 
            e.printStackTrace(); 
            new Alert(Alert.AlertType.ERROR, "Error al cargar la vista: " + fxml).show();
        }
    }
}