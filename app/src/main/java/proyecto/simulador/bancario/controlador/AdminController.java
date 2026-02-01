package proyecto.simulador.bancario.controlador;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.List;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import proyecto.simulador.bancario.Service.ClienteService;
import proyecto.simulador.bancario.Service.UsuarioService;
import proyecto.simulador.bancario.DAO.AdminDAO;
import proyecto.simulador.bancario.DAO.UsuarioDAO;
import proyecto.simulador.bancario.modelo.Usuario;
import proyecto.simulador.bancario.modelo.Cliente;

public class AdminController {

    // FXML: Componentes de Registro
    @FXML private TextField txtNewUser, txtNewPass;
    @FXML private TextField txtPNombre, txtSNombre, txtPApellido, txtSApellido;
    @FXML private TextField txtCedula, txtEmail, txtTelefono, txtDireccion;
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cbSexo;

    // FXML: Componentes de Dashboard
    @FXML private Label lblDineroTotal;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername, colRol;

    @FXML private Label lblTotalUsuarios;    // Para el conteo de usuarios
    @FXML private Label lblUsuariosBloqueados;  // Para saber cuántos están bloqueados

    // Servicios y DAOs
    private final AdminDAO adminDAO = new AdminDAO();
    private final ClienteService clienteService = new ClienteService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioService usuarioService = new UsuarioService();
    private java.util.Map<Integer, String> mapaEstadosClientes = new java.util.HashMap<>();
    
    @FXML
    public void initialize() {
        // 1. VINCULACIÓN DE COLUMNAS
        // Usamos Lambdas para extraer los datos de cada objeto Usuario
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getIdUsuario()));
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
        colRol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRol().name()));

        // 2. LÓGICA VISUAL: COLORES Y ESTADOS EN LA TABLA
        colRol.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Usuario user = getTableView().getItems().get(getIndex());
                    setText(item);
                    
                    // Consultamos el mapa de estados que cargaremos del DAO
                    String estado = mapaEstadosClientes.getOrDefault(user.getIdUsuario(), "ACTIVO");

                    if ("CLIENTE".equals(item)) {
                        if ("BLOQUEADO".equals(estado)) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            setText(item + " (BLOQUEADO)");
                        } else {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            setText(item + " (ACTIVO)");
                        }
                    } else {
                        // Estilo para el Administrador
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // 3. MENÚ CONTEXTUAL (Clic Derecho)
        ContextMenu menuTabla = new ContextMenu();
        MenuItem itemVerDatos = new MenuItem("Ver Datos Personales");
        MenuItem itemBloquear = new MenuItem("Bloquear/Activar Cliente");
        MenuItem itemEliminar = new MenuItem("Eliminar Usuario y Cliente");

        // Asignación de acciones a los items del menú
        itemVerDatos.setOnAction(e -> {
            Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (seleccionado != null) mostrarDetallesCliente(seleccionado);
        });
        itemBloquear.setOnAction(e -> gestionarEstadoCliente());
        itemEliminar.setOnAction(e -> eliminarUsuarioSeleccionado());

        menuTabla.getItems().addAll(itemVerDatos, new SeparatorMenuItem(), itemBloquear, itemEliminar);
        tablaUsuarios.setContextMenu(menuTabla);

        // 4. DOBLE CLIC (Acceso rápido a detalles)
        tablaUsuarios.setOnMouseClicked(event -> {
            Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && seleccionado != null) {
                mostrarDetallesCliente(seleccionado);
            }
        });

        // 5. INICIALIZAR COMPONENTES DE REGISTRO
        if (cbSexo != null) {
            cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        }

        // 6. CARGAR DATOS REALES DESDE LA DB
        // Este método es vital, si no se llama, la tabla sale vacía
        refrescarTablaYMapa(); 
        
        System.out.println("DEBUG: AdminController inicializado y datos cargados.");
    }

    private void refrescarTablaYMapa() {
        // Traemos los estados actuales para que el CellFactory sepa qué colores poner
        this.mapaEstadosClientes = adminDAO.obtenerEstadosClientes();
        
        // Traemos la lista de usuarios
        List<Usuario> lista = adminDAO.listarTodosLosUsuarios();
        if (lista != null) {
            tablaUsuarios.setItems(FXCollections.observableArrayList(lista));
        }
        
        // Actualizamos las etiquetas superiores (Capital, Total, Bloqueados)
        actualizarDashboard();
    }

    private void gestionarEstadoCliente() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        // Obtener estado actual del mapa que ya tienes
        String estadoActual = mapaEstadosClientes.getOrDefault(seleccionado.getIdUsuario(), "ACTIVO");
        String nuevoEstado = estadoActual.equals("ACTIVO") ? "BLOQUEADO" : "ACTIVO";

        // Confirmación rápida
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cambiar Estado");
        alert.setHeaderText("¿Deseas cambiar el estado a " + nuevoEstado + "?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (adminDAO.cambiarEstadoCliente(seleccionado.getIdUsuario(), nuevoEstado)) {
                // Actualizamos el mapa local para que la tabla cambie de color de inmediato
                mapaEstadosClientes.put(seleccionado.getIdUsuario(), nuevoEstado);
                tablaUsuarios.refresh(); 
                actualizarDashboard(); // Si tienes un contador de bloqueados, se actualiza
            }
        }
    }

    private void mostrarDetallesCliente(Usuario usuario) {
        try {
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/DetalleClienteView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y pasarle los datos
            DetalleClienteController controller = loader.getController();
            // Usamos el ID del usuario seleccionado
            controller.cargarDatos(usuario.getIdUsuario(), usuario.getUsername());

            // Crear y mostrar la escena
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalles: " + usuario.getUsername());
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de detalles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void actualizarDashboard() {
        // Total de dinero en el banco
        BigDecimal patrimonio = adminDAO.obtenerPatrimonioTotal();
        lblDineroTotal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(patrimonio));
        
        // Contar usuarios desde la lista de la tabla
        long totalUsuarios = tablaUsuarios.getItems().size();
        lblTotalUsuarios.setText(String.valueOf(totalUsuarios));

        // Contar bloqueados usando el mapa
        long bloqueados = mapaEstadosClientes.values().stream()
                            .filter(e -> e.equalsIgnoreCase("BLOQUEADO"))
                            .count();
        lblUsuariosBloqueados.setText(String.valueOf(bloqueados));
    }

    private void eliminarUsuarioSeleccionado() {
         Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            // Confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que deseas eliminar a " + seleccionado.getUsername() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    usuarioDAO.eliminarUsuario(seleccionado.getIdUsuario());
                     actualizarDashboard(); // Refrescar tabla
                }
            });
        } else {
            mostrarAlerta("Atención", "Selecciona un usuario de la tabla primero.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void onGuardarNuevoCliente() {
        int idGenerado = -1;
        try {
            // 1. Validaciones de interfaz
            validarCamposObligatorios();
            
            // 2. Validación de lógica de negocio (UsuarioService)
            usuarioService.validarUsuario(txtNewUser.getText(), txtNewPass.getText());

            // 3. PASO 1: Crear el Usuario en la BD
            Usuario nuevoUsuario = new Usuario(0, txtNewUser.getText(), txtNewPass.getText(), Usuario.Rol.CLIENTE);
            idGenerado = usuarioDAO.crearUsuario(nuevoUsuario); 

            // 4. PASO 2: Crear el Cliente vinculado
            clienteService.crearCliente(
                txtPNombre.getText(), txtSNombre.getText(), 
                txtPApellido.getText(), txtSApellido.getText(),
                cbSexo.getValue(), dpFechaNac.getValue(), 
                txtCedula.getText(), txtEmail.getText(),
                txtTelefono.getText(), txtDireccion.getText(), 
                Cliente.Estado.ACTIVO, idGenerado
            );

            mostrarAlerta("Éxito", "Administrador: El cliente ha sido registrado correctamente.", Alert.AlertType.INFORMATION);
            limpiarCampos();
            actualizarDashboard();

        } catch (IllegalArgumentException e) {
            // Errores de validación (Edad, campos vacíos, etc.)
            if (idGenerado != -1) usuarioDAO.eliminarUsuario(idGenerado);
            mostrarAlerta("Validación", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            // Errores de SQL o Sistema
            if (idGenerado != -1) usuarioDAO.eliminarUsuario(idGenerado);
            mostrarAlerta("Error de Sistema", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    public void onLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/LoginView.fxml"));
            Stage stage = (Stage) lblDineroTotal.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Banco");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo volver al login", Alert.AlertType.ERROR);
        }
    }

    private void validarCamposObligatorios() {
        if (txtNewUser.getText().isEmpty() || txtPNombre.getText().isEmpty() || 
            txtPApellido.getText().isEmpty() || txtCedula.getText().isEmpty() || 
            dpFechaNac.getValue() == null) {
            throw new IllegalArgumentException("Por favor, llene todos los campos obligatorios.");
        }
    }


    private void limpiarCampos() {
        txtNewUser.clear(); txtNewPass.clear();
        txtPNombre.clear(); txtSNombre.clear();
        txtPApellido.clear(); txtSApellido.clear();
        txtCedula.clear(); txtEmail.clear();
        txtTelefono.clear(); txtDireccion.clear();
        dpFechaNac.setValue(null);
        cbSexo.setValue(null);
    }

    private void mostrarAlerta(String titulo, String msj, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}
