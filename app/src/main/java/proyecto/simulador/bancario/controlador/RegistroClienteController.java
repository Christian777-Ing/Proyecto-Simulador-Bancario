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
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.Service.UsuarioService;
import proyecto.simulador.bancario.Service.CuentaService;

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

    private final CuentaService cuentaService = new CuentaService();

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
            // 1. Validar que los campos no estén vacíos
            validarCampos();

            // 2. Crear el Perfil de Cliente
            clienteService.crearCliente(
                    txtPNombre.getText().trim(), txtSNombre.getText().trim(),
                    txtPApellido.getText().trim(), txtSApellido.getText().trim(),
                    cbSexo.getValue(), dpFecha.getValue(),
                    txtCedula.getText().trim(), txtEmail.getText().trim(),
                    txtTelefono.getText().trim(), txtDir.getText().trim(),
                    Cliente.Estado.ACTIVO, idUsuarioVinculado
            );

            // 3. Obtener el ID del cliente recién creado para vincular la cuenta
            Cliente nuevoCliente = clienteService.obtenerClientePorUsuario(idUsuarioVinculado);

            if (nuevoCliente != null) {
                // 4. Crear la cuenta de ahorros inicial automáticamente
                cuentaService.crearCuenta(nuevoCliente.getIdCliente(), Cuenta.Tipo.AHORROS);
                
                mostrarAlerta(Alert.AlertType.INFORMATION, "¡Éxito!", 
                    "Perfil creado y Cuenta de Ahorros activada correctamente.");
            }

            irAlLogin();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", "No se pudo completar el registro: " + e.getMessage());
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