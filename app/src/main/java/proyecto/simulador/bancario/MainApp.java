package proyecto.simulador.bancario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Apuntamos a la nueva vista de Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LoginView.fxml"));
        Parent root = loader.load();

        // 2. Configuramos la escena
        Scene scene = new Scene(root);
        
        // 3. Configuramos el escenario (ventana) principal
        primaryStage.setTitle("Simulador Bancario - Inicio de Sesión");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); 
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
