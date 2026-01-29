package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.CuentaService;
import proyecto.simulador.bancario.modelo.Cuenta;
import java.math.BigDecimal;

public class OperacionController {
    @FXML private Label lblTitulo, lblCuentaInfo;
    @FXML private TextField txtMonto;

    private Cuenta cuentaSeleccionada;
    private boolean esDeposito;
    private final CuentaService cuentaService = new CuentaService();

    public void initData(Cuenta cuenta, boolean esDeposito) {
        this.cuentaSeleccionada = cuenta;
        this.esDeposito = esDeposito;
        lblTitulo.setText(esDeposito ? "Realizar Depósito" : "Realizar Retiro");
        lblCuentaInfo.setText("Cuenta: " + cuenta.getNumeroCuenta());
    }

    @FXML
    public void onConfirmar() {
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            if (esDeposito) {
                cuentaService.depositar(cuentaSeleccionada.getIdCuenta(), monto);
            } else {
                cuentaService.retirar(cuentaSeleccionada.getIdCuenta(), monto);
            }
            onCancelar(); // Cierra la ventana
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML public void onCancelar() {
        ((Stage) txtMonto.getScene().getWindow()).close();
    }
}
