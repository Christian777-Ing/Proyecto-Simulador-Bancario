package proyecto.simulador.bancario.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import proyecto.simulador.bancario.modelo.Transaccion;
import proyecto.simulador.bancario.Service.CuentaService;

import java.math.BigDecimal;
import java.util.List;

public class HistorialController {
    @FXML private TableView<Transaccion> tablaTransacciones;
    @FXML private TableColumn<Transaccion, String> colFecha, colTipo, colDestino;
    @FXML private TableColumn<Transaccion, BigDecimal> colMonto;

    private final CuentaService service = new CuentaService();
    private int idCuentaActual; // Guardamos el ID de la cuenta que estamos viendo

    public void cargarDatos(int idCuenta) {
        this.idCuentaActual = idCuenta; // Guardamos el contexto
        List<Transaccion> lista = service.verHistorial(idCuenta);
        
        // 1. Configuración de Fecha y Tipo
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().toString()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().toString()));

        // 2. Columna Destino Dinámica (Norman: Retroalimentación clara)
        colDestino.setCellValueFactory(c -> {
            Transaccion transaccion = c.getValue();
            if (transaccion.getTipo() == Transaccion.Tipo.TRANSFERENCIA) {
                // Si nosotros enviamos, mostramos a quién. Si recibimos, mostramos de quién.
                return new SimpleStringProperty(
                    transaccion.getIdCuentaOrigen() == idCuentaActual ? "Para: " + transaccion.getNumeroCuentaDestino() 
                                                           : "De: " + transaccion.getNumeroCuentaOrigen()
                );
            }
            return new SimpleStringProperty("-"); // Para depósitos/retiros
        });

        // 3. Monto con Colores y Signos (Nielsen #4: Consistencia visual)
        colMonto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getMonto()));
        colMonto.setCellFactory(column -> new TableCell<Transaccion, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal monto, boolean empty) {
                super.updateItem(monto, empty);
                if (empty || monto == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaccion t = getTableView().getItems().get(getIndex());
                    
                    // Lógica de salida de dinero (RETIRO o TRANSFERENCIA ORIGEN)
                    boolean esSalida = t.getTipo() == Transaccion.Tipo.RETIRO || 
                                      (t.getTipo() == Transaccion.Tipo.TRANSFERENCIA && t.getIdCuentaOrigen() == idCuentaActual);

                    if (esSalida) {
                        setText(String.format("- $ %.2f", monto));
                        setTextFill(Color.RED); // Norman: El rojo indica disminución
                    } else {
                        setText(String.format("+ $ %.2f", monto));
                        setTextFill(Color.DARKGREEN); // El verde indica aumento
                    }
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        tablaTransacciones.setItems(FXCollections.observableArrayList(lista));
    }
}
