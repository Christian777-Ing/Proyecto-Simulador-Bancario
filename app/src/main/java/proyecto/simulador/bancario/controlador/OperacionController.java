package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.CuentaService;
import proyecto.simulador.bancario.modelo.Cuenta;
import java.math.BigDecimal;


public class OperacionController {

    @FXML private Label lblTitulo;
    @FXML private Label lblCuentaInfo;
    @FXML private TextField txtMonto;

    private Cuenta cuenta;
    private boolean esDeposito;
    private final CuentaService service = new CuentaService();

    // Este método se llama desde CuentaController antes de mostrar la ventana
    public void initData(Cuenta cuenta, boolean esDeposito) {
        this.cuenta = cuenta;
        this.esDeposito = esDeposito;
        
        // Heurística #1: Visibilidad del estado del sistema
        if (esDeposito) {
            lblTitulo.setText("DEPOSITAR DINERO");
            lblTitulo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde éxito
            txtMonto.setPromptText("Monto a depositar");
        } else {
            lblTitulo.setText("RETIRAR DINERO");
            lblTitulo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rojo advertencia
            txtMonto.setPromptText("Monto a retirar");
        }
        
        // Proporciona contexto claro (Principio de Norman: Conocimiento en el mundo)
        lblCuentaInfo.setText("Cuenta seleccionada: " + cuenta.getNumeroCuenta());
        
        // Heurística #5: Prevención de errores (solo números y un punto)
        txtMonto.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d*(\\.\\d*)?")) {
                txtMonto.setText(oldV);
            }
        });
    }

    @FXML
    public void onConfirmar() {
        try {
            // Validación de campo vacío (Heurística #5: Prevención)
            if (txtMonto.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Por favor, ingrese un monto para continuar.");
            }
            
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto debe ser un valor positivo.");
            }

            if (esDeposito) {
                service.depositar(cuenta.getIdCuenta(), monto);
                // Retroalimentación (Norman): El usuario sabe que la acción fue exitosa
                mostrarAlerta(Alert.AlertType.INFORMATION, "Depósito Exitoso", 
                            "Se han depositado $" + monto + " a la cuenta " + cuenta.getNumeroCuenta());
            } else {
                // Validación de saldo (Prevención de errores antes de fallar en BD)
                if (monto.compareTo(cuenta.getSaldo()) > 0) {
                    throw new Exception("Saldo insuficiente. Su saldo actual es: $" + cuenta.getSaldo());
                }
                
                service.retirar(cuenta.getIdCuenta(), monto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Retiro Exitoso", 
                            "Ha retirado $" + monto + " de su cuenta.");
            }

            cerrarVentana();
            
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Incorrecto", "El monto ingresado no es un número válido.");
        } catch (Exception e) {
            // Heurística #9: Mensajes de error claros sin códigos técnicos
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo realizar la operación", e.getMessage());
        }
    }

    @FXML
    public void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtMonto.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
