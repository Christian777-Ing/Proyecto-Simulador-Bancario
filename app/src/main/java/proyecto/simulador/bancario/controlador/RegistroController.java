package proyecto.simulador.bancario.controlador;

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

public class RegistroController {
    @FXML private TextField txtUser, txtNombre, txtCedula, txtEmail;
    @FXML private PasswordField txtPass;
    private final UsuarioDAO uDao = new UsuarioDAO();
    private final ClienteService cServ = new ClienteService();

    @FXML
    public void onRegistrar() {
        try {
            Usuario u = new Usuario(0, txtUser.getText(), txtPass.getText(), Usuario.Rol.CLIENTE);
            uDao.crearUsuario(u);
            cServ.crearCliente(txtNombre.getText(), txtCedula.getText(), txtEmail.getText(), "S/N", "S/D", Cliente.Estado.ACTIVO, u.getIdUsuario());
            new Alert(Alert.AlertType.INFORMATION, "Registro Exitoso").showAndWait();
            onVolver();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void onVolver() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
            ((Stage) txtUser.getScene().getWindow()).setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
