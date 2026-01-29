package proyecto.simulador.bancario.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.LoginService;
import proyecto.simulador.bancario.Service.CuentaService;
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.modelo.Transaccion;
import proyecto.simulador.bancario.modelo.Usuario;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;
import java.util.List;

public class CuentaController {

    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, Integer> colId;
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, Cuenta.Tipo> colTipo;
    @FXML private TableColumn<Cuenta, BigDecimal> colSaldo;
    @FXML private TableColumn<Cuenta, Cuenta.Estado> colEstado;

    private final CuentaService service = new CuentaService();

    @FXML
    public void initialize() {
        // Configuración de celdas
        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getIdCuenta()).asObject());
        colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumeroCuenta()));
        colTipo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTipo()));
        colSaldo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSaldo()));
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstado()));

        cargarCuentas();
    }

    private void cargarCuentas() {
        // 1. Obtenemos el usuario de la sesión actual
        Usuario logueado = LoginService.getUsuarioLogueado();
        
        if (logueado != null) {
            // 2. Cargamos las cuentas usando el ID del usuario
            // Nota: Si tu DB requiere idCliente, asegúrate de que tu service haga el cruce
            List<Cuenta> cuentas = service.obtenerCuentasCliente(logueado.getIdUsuario());
            tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
        }
    }

    @FXML
    public void onDepositar() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            abrirVentanaOperacion(seleccionada, true);
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona una cuenta.");
        }
    }

    @FXML
    public void onRetirar() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            abrirVentanaOperacion(seleccionada, false);
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona una cuenta.");
        }
    }

    private void abrirVentanaOperacion(Cuenta cuenta, boolean esDeposito) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/OperacionView.fxml"));
            Parent root = loader.load();
            
            OperacionController controller = loader.getController();
            controller.initData(cuenta, esDeposito);
            
            Stage stage = new Stage();
            stage.setTitle(esDeposito ? "Depósito" : "Retiro");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Pausa la ejecución hasta que se cierre la ventana
            
            cargarCuentas(); // Refresca la tabla automáticamente al cerrar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onTransferir() {
        Cuenta origen = tablaCuentas.getSelectionModel().getSelectedItem();
        if (origen != null) {
            // Aquí podrías abrir una ventana similar a OperacionView pero con campo "Cuenta Destino"
            System.out.println("Abriendo ventana de transferencia...");
        }
    }

    @FXML
    public void onVerHistorial() {
        Cuenta cuenta = tablaCuentas.getSelectionModel().getSelectedItem();
        if (cuenta != null) {
            // Lógica para abrir una nueva vista con la lista de transacciones
            List<Transaccion> historial = service.verHistorial(cuenta.getIdCuenta());
            historial.forEach(t -> System.out.println(t.getTipo() + ": " + t.getMonto()));
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
