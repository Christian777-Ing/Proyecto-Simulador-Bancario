package proyecto.simulador.bancario.controlador;


import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import proyecto.simulador.bancario.DAO.AdminDAO;
import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.modelo.Cuenta;

import javafx.fxml.Initializable;

public class DetalleClienteController implements Initializable {
    @FXML private Label lblNombre, lblCedula, lblEstado, lblEmail, lblTelefono, lblDireccion, lblSexo;  
    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, String> colNumero, colTipo;
    @FXML private TableColumn<Cuenta, BigDecimal> colSaldo;


    private AdminDAO adminDAO = new AdminDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Configuración de celdas básicas
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCuenta"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        // 2. Formateador de Moneda (USA o el de tu país)
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

        // 3. Aplicar formato y color a la columna Saldo
        colSaldo.setCellFactory(column -> new TableCell<Cuenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatoMoneda.format(saldo));
                    // Alineación a la derecha (estilo contable)
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                    
                    // Color dinámico: Verde si tiene dinero, Rojo si está en deuda
                    if (saldo.compareTo(BigDecimal.ZERO) >= 0) {
                        setStyle(getStyle() + "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle(getStyle() + "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        System.out.println("DEBUG: Columnas configuradas con formato de moneda.");
    }
    
    public void cargarDatos(int idUsuario, String username) {
        // 1. Cargar datos del Cliente
        Cliente cliente = adminDAO.obtenerDetallesCliente(idUsuario);
        if (cliente != null) {
            lblNombre.setText(cliente.getPrimerNombre() + " " + cliente.getSegundoNombre() + " " + cliente.getPrimerApellido() + " " + cliente.getSegundoApellido());
            lblCedula.setText(cliente.getCedula());
            lblSexo.setText(cliente.getSexo());
            lblEmail.setText(cliente.getEmail());
            lblTelefono.setText(cliente.getTelefono());
            lblDireccion.setText(cliente.getDireccion());
            lblEstado.setText(adminDAO.obtenerEstadoClientePorUsuario(idUsuario));
        }

        // 2. Cargar Cuentas en la Tabla
        List<Cuenta> cuentas = adminDAO.listarCuentasPorUsuario(idUsuario);
        
        if (cuentas == null) {
            System.out.println("DEBUG: La lista de cuentas es NULA (Error en el DAO)");
        } else if (cuentas.isEmpty()) {
            System.out.println("DEBUG: El usuario ID " + idUsuario + " no tiene cuentas en la DB");
        } else {
            System.out.println("DEBUG: Se encontraron " + cuentas.size() + " cuentas. Enviando a la tabla...");
            tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
            tablaCuentas.refresh(); // Forzar renderizado
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) lblNombre.getScene().getWindow();
        stage.close();
    }
}
