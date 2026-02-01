package proyecto.simulador.bancario.controlador;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.Service.CuentaService;
import java.math.BigDecimal;

public class TransferenciaController {
    @FXML private Label lblCuentaOrigen;
    @FXML private Label lblSaldoOrigen;
    @FXML private TextField txtCuentaDestino;
    @FXML private TextField txtMonto;
    @FXML private TextField txtCedulaDestino;
    @FXML private Label lblNombreDestinatario;
    @FXML private Button btnConfirmar;
    @FXML private VBox paneValidacion;
    
    private Cuenta cuentaOrigen;
    private final CuentaService service = new CuentaService();

    // Método para recibir los datos de la cuenta que envía
    public void initData(Cuenta origen) {
        this.cuentaOrigen = origen;
        lblCuentaOrigen.setText(origen.getNumeroCuenta());
        lblSaldoOrigen.setText(String.format("Saldo disponible: $ %.2f", origen.getSaldo()));
        
        // Restricción para el campo de monto
        txtMonto.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*(\\.\\d*)?")) {
                txtMonto.setText(oldV);
            }
        });

        // --- NUEVO: Listeners para invalidar si el usuario cambia el texto ---
        txtCuentaDestino.textProperty().addListener((obs, oldV, newV) -> invalidarConfirmacion());
        txtCedulaDestino.textProperty().addListener((obs, oldV, newV) -> invalidarConfirmacion());
    }

    private void invalidarConfirmacion() {
        btnConfirmar.setDisable(true);
        paneValidacion.setVisible(false);
        lblNombreDestinatario.setText("");
    }

    @FXML
    public void onConfirmarTransferencia() {
        try {
            // 1. Capturamos los datos de la interfaz
            String numDestino = txtCuentaDestino.getText().trim();
            
            // Validación básica de entrada (Nielsen #5: Prevención de errores)
            if (numDestino.isEmpty()) {
                throw new Exception("Debe ingresar un número de cuenta destino.");
            }

            BigDecimal monto;
            try {
                monto = new BigDecimal(txtMonto.getText());
            } catch (NumberFormatException e) {
                throw new Exception("El monto debe ser un valor numérico válido.");
            }

            // 2. LLAMADA CORRECTA AL SERVICE
            // Usamos transferirPorNumero para que el Service se encargue de buscar el ID
            service.transferirPorNumero(cuentaOrigen.getIdCuenta(), numDestino, monto);

            // 3. Feedback de éxito (Norman: Retroalimentación)
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Transferencia Exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Se han transferido $" + monto + " a la cuenta " + numDestino);
            alerta.showAndWait();

            cerrarVentana();

        } catch (Exception e) {
            // Aquí capturamos el error si la cuenta no existe o hay saldo insuficiente
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error en la Operación");
            alerta.setHeaderText(null);
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }
    
    @FXML
    public void onCancelar() {
        // Principio de Norman: Salida fácil y segura para el usuario
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtMonto.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onVerificarDestinatario() {
        String cuenta = txtCuentaDestino.getText().trim();
        String cedula = txtCedulaDestino.getText().trim();

        try {
            String nombre = service.validarTitular(cuenta, cedula);
            
            if (nombre != null) {
                lblNombreDestinatario.setText(nombre);
                paneValidacion.setVisible(true);
                paneValidacion.setManaged(true); // Asegura que ocupe espacio
                btnConfirmar.setDisable(false);
                System.out.println("Validación exitosa: " + nombre);
            }
        } catch (Exception e) {
            System.out.println("Error en validación: " + e.getMessage());
            mostrarAlerta("Error de validación", e.getMessage());
            paneValidacion.setVisible(false);
            btnConfirmar.setDisable(true);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
