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

public class RegistroClienteController {

    @FXML private TextField txtPNombre, txtSNombre, txtPApellido, txtSApellido;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtCedula, txtEmail, txtTelf, txtDir;
    @FXML private ComboBox<String> cbSexo;

    private final ClienteService cServ = new ClienteService();
    
    // Esta variable guardará el ID que viene del controlador de Usuario
    private int idUsuarioVinculado;

    // Método para recibir el ID desde RegistroUsuarioController
    public void setIdUsuarioVinculado(int id) {
        this.idUsuarioVinculado = id;
    }

    @FXML
    public void initialize() {
        cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        
        // Restricción visual de calendario (18 a 130 años)
        LocalDate hace18Anios = LocalDate.now().minusYears(18);
        LocalDate hace130Anios = LocalDate.now().minusYears(130);
        dpFecha.setValue(hace18Anios);

        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(hace18Anios) || date.isBefore(hace130Anios));
            }
        });
    }

    @FXML
    public void onRegistrar() {
        try {
            // Validaciones locales antes de intentar guardar
            validarCampos();

            // Registro del cliente usando el ID del usuario creado previamente
            cServ.crearCliente(
                txtPNombre.getText(), txtSNombre.getText(), txtPApellido.getText(), txtSApellido.getText(),
                cbSexo.getValue(), dpFecha.getValue(), txtCedula.getText(), txtEmail.getText(),
                txtTelf.getText(), txtDir.getText(), Cliente.Estado.ACTIVO, idUsuarioVinculado
            );

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Perfil de cliente creado correctamente.");
            irAlLogin();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato Incorrecto", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", e.getMessage());
        }
    }

    private void validarCampos() {
        if (txtPNombre.getText().isBlank()) throw new IllegalArgumentException("El primer nombre es obligatorio.");
        if (txtPApellido.getText().isBlank()) throw new IllegalArgumentException("El primer apellido es obligatorio.");
        if (txtCedula.getText().length() != 10) throw new IllegalArgumentException("La cédula debe tener 10 dígitos.");
        if (dpFecha.getValue() == null) throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        if (cbSexo.getValue() == null) throw new IllegalArgumentException("Debe seleccionar el sexo.");
    }

    private void irAlLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
            Stage stage = (Stage) txtPNombre.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}