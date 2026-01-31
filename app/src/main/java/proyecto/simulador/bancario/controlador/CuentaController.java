package proyecto.simulador.bancario.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.LoginService;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.Service.CuentaService;
import proyecto.simulador.bancario.modelo.Cliente;
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
    // Eliminada colId porque no existe en tu FXML
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, Cuenta.Tipo> colTipo;
    @FXML private TableColumn<Cuenta, BigDecimal> colSaldo;
    @FXML private TableColumn<Cuenta, Cuenta.Estado> colEstado;

    private final CuentaService service = new CuentaService();

    @FXML
    public void initialize() {
        // Configuración de celdas - Asegúrate que los fx:id en el FXML coincidan
        colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumeroCuenta()));
        colTipo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTipo()));
        colSaldo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSaldo()));
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstado()));

        cargarCuentas();
    }

    private void cargarCuentas() {
        Usuario logueado = LoginService.getUsuarioLogueado();
        if (logueado == null) return;

        ClienteService clienteService = new ClienteService();
        Cliente cliente = clienteService.obtenerClientePorUsuario(logueado.getIdUsuario());

        if (cliente != null) {
            List<Cuenta> cuentas = service.obtenerCuentasCliente(cliente.getIdCliente());
            tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
        }
    }

    @FXML
    public void onDepositar() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) abrirVentanaOperacion(seleccionada, true);
    }

    @FXML
    public void onRetirar() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) abrirVentanaOperacion(seleccionada, false);
    }

    @FXML
    public void onVerHistorial() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            System.out.println("Cargando historial para: " + seleccionada.getNumeroCuenta());
            // Aquí iría la lógica para abrir la vista de historial
        }
    }

    private void abrirVentanaOperacion(Cuenta cuenta, boolean esDeposito) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/OperacionView.fxml"));
            Parent root = loader.load();
            // ... lógica de inicialización de operación
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            cargarCuentas(); 
        } catch (Exception e) { e.printStackTrace(); }
    }
}
