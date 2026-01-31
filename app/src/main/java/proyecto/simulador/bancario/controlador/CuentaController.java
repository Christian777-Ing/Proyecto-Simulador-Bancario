package proyecto.simulador.bancario.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
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

import java.io.IOException;
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
        // 1. Configuración de celdas básica
        colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumeroCuenta()));
        colTipo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTipo()));
        colSaldo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSaldo()));
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstado()));

        // 2. Formateo visual de la columna Saldo (Símbolo $ y Color)
        colSaldo.setCellFactory(column -> new TableCell<Cuenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                    setStyle(""); 
                } else {
                    // Formato: $ 1,250.00
                    setText(String.format("$ %.2f", saldo));
                    // Le damos un toque elegante en verde
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        // 3. Cargar los datos de la BD
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
        if (seleccionada == null) {
            // Heurística #9: Ayudar a reconocer el error
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", 
                        "Por favor, selecciona una cuenta de la lista para continuar.");
            return;
        }
        abrirVentanaOperacion(seleccionada, true);
    }

    @FXML
    public void onRetirar() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", 
                        "Selecciona una cuenta para retirar dinero.");
            return;
        }
        // El segundo parámetro DEBE ser false para que sea Retiro
        abrirVentanaOperacion(seleccionada, false); 
    }


    @FXML
    public void onVerHistorial() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", 
                        "Por favor, selecciona una cuenta para ver sus movimientos.");
            return;
        }

        try {
            // 1. Cargar el FXML del historial
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/HistorialView.fxml"));
            Parent root = loader.load();
            
            // 2. Obtener su controlador y cargar los datos
            HistorialController controller = loader.getController();
            controller.cargarDatos(seleccionada.getIdCuenta());

            // 3. Configurar la ventana (Stage)
            Stage stage = new Stage();
            stage.setTitle("Movimientos - Cuenta N° " + seleccionada.getNumeroCuenta());
            stage.initModality(Modality.APPLICATION_MODAL); // Enfoque de Norman
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el historial.");
        }
    }

    private void abrirVentanaOperacion(Cuenta cuenta, boolean esDeposito) {
        try {
            // Usa la ruta exacta desde la raíz de resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/OperacionView.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y pasar datos ANTES de mostrar la ventana
            OperacionController controller = loader.getController();
            controller.initData(cuenta, esDeposito);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Norman: Evita distracciones (Modo Diálogo)
            stage.setTitle(esDeposito ? "Depósito" : "Retiro");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            cargarCuentas(); // Actualiza la tabla al cerrar
            
        } catch (Exception e) {
            // Heurística #9: Ayuda al usuario a entender el error
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo abrir la ventana de operación.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onNuevaCuenta() {
        // 1. Crear opciones para el usuario
        List<Cuenta.Tipo> opciones = List.of(Cuenta.Tipo.AHORROS, Cuenta.Tipo.CORRIENTE);
        ChoiceDialog<Cuenta.Tipo> dialog = new ChoiceDialog<>(Cuenta.Tipo.AHORROS, opciones);
        dialog.setTitle("Nueva Cuenta");
        dialog.setHeaderText("Apertura de cuenta bancaria");
        dialog.setContentText("Seleccione el tipo de cuenta:");

        dialog.showAndWait().ifPresent(tipo -> {
            try {
                // 2. Obtener el cliente logueado
                Usuario logueado = LoginService.getUsuarioLogueado();
                ClienteService clienteService = new ClienteService();
                Cliente cliente = clienteService.obtenerClientePorUsuario(logueado.getIdUsuario());

                // 3. Crear la cuenta
                service.crearCuenta(cliente.getIdCliente(), tipo);
                
                // 4. Refrescar la tabla
                cargarCuentas();
                
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Se ha creado su nueva cuenta de " + tipo);
                
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo crear la cuenta: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onTransferir() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            // Heurística #5: Prevención de errores
            mostrarAlerta("Selección Requerida", "Por favor, selecciona una cuenta de origen para transferir.");
            return;
        }
        abrirVentanaTransferencia(seleccionada);
    }

    private void abrirVentanaTransferencia(Cuenta origen) {
        try {
            // ¡OJO! Verifica que el nombre del archivo sea exactamente TransferenciaView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/TransferenciasView.fxml"));
            Parent root = loader.load();
            
            // Pasar la cuenta al controlador de transferencia
            TransferenciaController controller = loader.getController();
            controller.initData(origen);

            Stage stage = new Stage();
            stage.setTitle("Transferencia Bancaria - " + origen.getNumeroCuenta());
            stage.initModality(Modality.APPLICATION_MODAL); // Enfoque de Norman
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            cargarCuentas(); // Refrescar la tabla al volver
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de carga", "No se pudo abrir la ventana de transferencia. Verifique la ruta del FXML.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        mostrarAlerta(Alert.AlertType.WARNING, titulo, mensaje);
    }

}
