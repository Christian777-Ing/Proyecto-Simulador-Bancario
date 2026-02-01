package proyecto.simulador.bancario.controlador;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.LoginService;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.Service.CuentaService;
import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.modelo.Transaccion;
import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.DAO.TransferenciaDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import java.io.File;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

public class CuentaController {

    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, Cuenta.Tipo> colTipo;
    @FXML private TableColumn<Cuenta, BigDecimal> colSaldo;
    @FXML private TableColumn<Cuenta, Cuenta.Estado> colEstado;
    @FXML private Button btnAdminPanel;
    @FXML private Label lblSaldoTotal;
    @FXML private TextField txtBuscar;

    // --- AÑADE ESTAS LÍNEAS PARA EL NUEVO FXML ---
    @FXML private Label lblNombreUsuario; 
    @FXML private Button btnDepositar;
    @FXML private Button btnRetirar;

    private final CuentaService service = new CuentaService();
    private final TransferenciaDAO tranfeDAO = new TransferenciaDAO();
    

    @FXML
    public void initialize() {
        // 1. Configuración de Columnas (Mapeo de datos)
        colNumero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumeroCuenta()));
        colTipo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTipo()));
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstado()));
        
        // 2. Formateo visual del Saldo (Heurística: Visibilidad del estado del sistema)
        colSaldo.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSaldo()));
        colSaldo.setCellFactory(column -> new TableCell<Cuenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                    setStyle(""); 
                } else {
                    setText(String.format("$ %.2f", saldo));
                    // Estilo verde esmeralda para saldos positivos
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        // 3. Carga Inicial de Datos y Perfil
        cargarCuentas(); 
        actualizarInformacionUsuario(); // Nueva función para el lblNombreUsuario

        // 4. Implementación del Buscador en Tiempo Real
        // Importante: Usamos una lista observable para que el filtrado sea reactivo
        FilteredList<Cuenta> filteredData = new FilteredList<>(tablaCuentas.getItems(), p -> true);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cuenta -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (cuenta.getNumeroCuenta().toLowerCase().contains(lowerCaseFilter)) return true;
                if (cuenta.getTipo().toString().toLowerCase().contains(lowerCaseFilter)) return true;
                if (cuenta.getEstado().toString().toLowerCase().contains(lowerCaseFilter)) return true;
                
                return false; 
            });
            actualizarSaldoTotalFiltrado(filteredData); // Actualiza el total según lo que ves en pantalla
        });

        // Vincular la lista filtrada con el ordenamiento de la tabla
        SortedList<Cuenta> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaCuentas.comparatorProperty());
        tablaCuentas.setItems(sortedData);

        // 5. UX: Foco inicial y Atajos
        Platform.runLater(() -> {
            tablaCuentas.requestFocus();
            if (!tablaCuentas.getItems().isEmpty()) {
                tablaCuentas.getSelectionModel().selectFirst();
            }
        });

        tablaCuentas.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (tablaCuentas.getSelectionModel().getSelectedItem() != null) {
                    onDepositar();
                }
            }
        });

        // 6. Seguridad y Totales
        configurarSegunRol();
        actualizarSaldoTotal(); 
    }

    /**
     * Método auxiliar para setear el nombre del usuario en el Sidebar del FXML
     */
    private void actualizarInformacionUsuario() {
        Usuario logueado = LoginService.getUsuarioLogueado();
        if (logueado != null && lblNombreUsuario != null) {
            // Ponemos el nombre en Mayúsculas para que combine con el estilo "BIENVENIDO"
            lblNombreUsuario.setText(logueado.getUsername().toUpperCase());
        }
    }

    /**
     * Mantiene el Dashboard de saldo actualizado incluso cuando el usuario filtra la tabla
     */
    private void actualizarSaldoTotalFiltrado(FilteredList<Cuenta> listaFiltrada) {
        BigDecimal total = listaFiltrada.stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblSaldoTotal.setText(String.format("$ %.2f", total));
    }
    private void configurarSegunRol() {
        Usuario logueado = LoginService.getUsuarioLogueado();
        if (logueado != null && "ADMIN".equals(logueado.getRol())) {
            btnAdminPanel.setVisible(true);
            btnAdminPanel.setManaged(true);
        }
    }

    private void cargarCuentas() {
        Usuario logueado = LoginService.getUsuarioLogueado();
        if (logueado == null) return;

        ClienteService clienteService = new ClienteService();
        Cliente cliente = clienteService.obtenerClientePorUsuario(logueado.getIdUsuario());

        if (cliente != null) {
            List<Cuenta> cuentas = service.obtenerCuentasCliente(cliente.getIdCliente());
            tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
            
            // Nueva línea para actualizar el dashboard superior
            actualizarSaldoTotal(); 
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
    public void onRefrescar() {
        cargarCuentas();
        actualizarSaldoTotal();
    }

    private void actualizarSaldoTotal() {
        BigDecimal total = tablaCuentas.getItems().stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblSaldoTotal.setText(String.format("$ %.2f", total));
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

    @FXML
    private void onDesactivarCuenta() {
        // 1. Obtener la cuenta seleccionada de la tabla
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", 
                        "Por favor, selecciona la cuenta que deseas desactivar.");
            return;
        }

        // 2. No permitir desactivar una cuenta que ya está inactiva
        if (seleccionada.getEstado() == Cuenta.Estado.CERRADA) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Esta cuenta ya se encuentra inactiva.");
            return;
        }

        // 3. Confirmación de seguridad
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Desactivación");
        confirmacion.setHeaderText("¿Desactivar cuenta N° " + seleccionada.getNumeroCuenta() + "?");
        confirmacion.setContentText("Esta acción impedirá realizar operaciones. Solo podrá reactivarse en ventanilla.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try {
                // Llamamos al servicio (que debería validar saldo $0)
                service.desactivarCuenta(seleccionada);
                
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La cuenta ha sido desactivada.");
                cargarCuentas(); // Refrescar la tabla para ver el cambio de estado
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }
    @FXML
    public void onAbrirAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Panel de Administración Global");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir el panel de administración.");
        }
    }

    @FXML
    public void onAbrirPerfil() {
        try {
            String ruta = "/View/PerfilView.fxml";
            java.net.URL url = getClass().getResource(ruta);
            
            if (url == null) {
                throw new RuntimeException("No se encontró el archivo FXML en: " + ruta);
            }

            FXMLLoader loader = new FXMLLoader(url);
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Mi Perfil de Usuario");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error crítico al cargar perfil:");
            e.printStackTrace();
            
            // Alerta visual para el usuario
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Sistema");
            alert.setHeaderText("No se pudo cargar la vista de Perfil");
            alert.setContentText("Verifica que PerfilView.fxml esté en la carpeta correcta.");
            alert.showAndWait();
        }
    }

    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea cerrar sesión?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Limpiar usuario en el servicio
                    LoginService.setUsuarioLogueado(null);
                    
                    // Volver al Login
                    Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
                    Stage stage = (Stage) tablaCuentas.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Simulador Bancario - Login");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void onGenerarPDF() {
        Cuenta seleccionada = tablaCuentas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Selecciona una cuenta para generar el PDF.");
            return;
        }

        List<Transaccion> historial = tranfeDAO.listarPorCuenta(seleccionada.getIdCuenta());
        
        // --- LÓGICA PARA LA RUTA DE DESCARGAS ---
        String home = System.getProperty("user.home");
        // Usamos File.separator para que funcione en Windows (\) y Linux/Mac (/)
        String ruta = home + File.separator + "Downloads" + File.separator + "Reporte_Cuenta_" + seleccionada.getNumeroCuenta() + ".pdf";
        
        try {
            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            // Colores institucionales
            DeviceRgb azulBancario = new DeviceRgb(0, 51, 102);
            DeviceRgb grisFondo = new DeviceRgb(245, 245, 245);

            // 1. Título con línea decorativa
            documento.add(new Paragraph("BANCO CENTRAL DE ESPOL - ESTADO DE CUENTA")
                    .setFontColor(azulBancario)
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT));
            
            documento.add(new Paragraph("______________________________________________________________________________")
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(10));

            // 2. Bloque de Información General
            Table infoHeader = new Table(2).useAllAvailableWidth();
            infoHeader.setMarginBottom(20);
            
            // Celda Izquierda: Datos del cliente/cuenta
            Cell infoCell = new Cell();
            infoCell.add(new Paragraph("Detalles del Cliente\n").setBold().setFontSize(12));
            infoCell.add(new Paragraph("Cuenta N°: " + seleccionada.getNumeroCuenta() + "\nTipo: " + seleccionada.getTipo()));
            infoCell.setBorder(Border.NO_BORDER);
            
            // Celda Derecha: Saldo destacado
            Cell saldoCell = new Cell()
                    .add(new Paragraph("SALDO TOTAL\n").setBold())
                    .add(new Paragraph("$" + seleccionada.getSaldo()).setFontSize(18).setFontColor(azulBancario).setBold())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBackgroundColor(grisFondo)
                    .setPadding(10)
                    .setBorder(new SolidBorder(azulBancario, 1));

            infoHeader.addCell(infoCell);
            infoHeader.addCell(saldoCell);
            documento.add(infoHeader);

            // 3. Tabla de Movimientos Estilizada
            documento.add(new Paragraph("HISTORIAL DE MOVIMIENTOS").setBold().setFontSize(14).setMarginBottom(10));
            
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{25, 50, 25})).useAllAvailableWidth();
            
            // Encabezados
            String[] titulos = {"FECHA", "CONCEPTO", "VALOR"};
            for (String t : titulos) {
                tabla.addHeaderCell(new Cell().add(new Paragraph(t).setFontColor(ColorConstants.WHITE).setBold())
                        .setBackgroundColor(azulBancario)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8));
            }

            // Datos con efecto cebra
            for (int i = 0; i < historial.size(); i++) {
                Transaccion trans = historial.get(i);
                boolean esPar = (i % 2 == 0);
                
                Cell cFecha = new Cell().add(new Paragraph(trans.getFecha().toString())).setTextAlignment(TextAlignment.CENTER);
                Cell cTipo = new Cell().add(new Paragraph(trans.getTipo().toString()));
                Cell cMonto = new Cell().add(new Paragraph("$" + trans.getMonto())).setTextAlignment(TextAlignment.RIGHT);

                if (!esPar) {
                    cFecha.setBackgroundColor(grisFondo);
                    cTipo.setBackgroundColor(grisFondo);
                    cMonto.setBackgroundColor(grisFondo);
                }

                tabla.addCell(cFecha.setPadding(5));
                tabla.addCell(cTipo.setPadding(5));
                tabla.addCell(cMonto.setPadding(5));
            }

            documento.add(tabla);
            
            // 4. Pie de página
            documento.add(new Paragraph("\nEste documento es un reporte oficial. Fecha de generación: " + java.time.LocalDateTime.now())
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

            documento.close();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El PDF se ha guardado en tu carpeta de Descargas:\n" + ruta);

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Generación", "No se pudo crear el archivo: " + e.getMessage());
            e.printStackTrace();
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
