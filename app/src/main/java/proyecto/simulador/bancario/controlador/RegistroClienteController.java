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

    // --------- CAMPOS FXML ----------
    @FXML private TextField txtPNombre;
    @FXML private TextField txtSNombre;
    @FXML private TextField txtPApellido;
    @FXML private TextField txtSApellido;
    @FXML private TextField txtCedula;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDir;

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbSexo;

    // --------- SERVICIO ----------
    private final ClienteService clienteService = new ClienteService();

    // --------- ID USUARIO VINCULADO ----------
    private int idUsuarioVinculado;

    public void setIdUsuarioVinculado(int idUsuario) {
        this.idUsuarioVinculado = idUsuario;
    }

    // --------- INITIALIZE ----------
    @FXML
    public void initialize() {
        inicializarComboSexo();
        configurarDatePicker();
        limitarCamposNumericos();
    }

    private void inicializarComboSexo() {
        cbSexo.setItems(FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro"
        ));
    }

    private void configurarDatePicker() {
        LocalDate hoy = LocalDate.now();
        LocalDate min = hoy.minusYears(130);
        LocalDate max = hoy.minusYears(18);

        dpFecha.setValue(max);

        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(max) || date.isBefore(min));
            }
        });
    }

    private void limitarCamposNumericos() {
        txtCedula.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d{0,10}")) {
                txtCedula.setText(oldV);
            }
        });

        txtTelefono.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*")) {
                txtTelefono.setText(oldV);
            }
        });
    }

    // --------- REGISTRO ----------
     @FXML
    public void onRegistrar() {
        try {
            clienteService.crearCliente(
                    txtPNombre.getText().trim(),
                    txtSNombre.getText().trim(),
                    txtPApellido.getText().trim(),
                    txtSApellido.getText().trim(),
                    cbSexo.getValue(),
                    dpFecha.getValue(),
                    txtCedula.getText().trim(),
                    txtEmail.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtDir.getText().trim(),
                    Cliente.Estado.ACTIVO,
                    idUsuarioVinculado
            );

            new Alert(Alert.AlertType.INFORMATION,
                    "Registro completado correctamente").showAndWait();

            irAlLogin();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }
    // --------- VALIDACIONES ----------
    private void validarCampos() {
        if (txtPNombre.getText().isBlank())
            throw new IllegalArgumentException("El primer nombre es obligatorio.");

        if (txtPApellido.getText().isBlank())
            throw new IllegalArgumentException("El primer apellido es obligatorio.");

        if (!txtCedula.getText().matches("\\d{10}"))
            throw new IllegalArgumentException("La cédula debe tener exactamente 10 dígitos.");

        if (cbSexo.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar el sexo.");

        if (dpFecha.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar la fecha de nacimiento.");
    }

    // --------- NAVEGACIÓN ----------
    private void irAlLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/View/LoginView.fxml")
            );
            Stage stage = (Stage) txtPNombre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------- ALERTAS ----------
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}