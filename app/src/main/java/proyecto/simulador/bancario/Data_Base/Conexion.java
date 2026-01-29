package proyecto.simulador.bancario.Data_Base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/bancoPersonal?useSSL=false&serverTimezone=UTC";
    private static final String USER = "bank_user";
    private static final String PASSWORD = "Choyi777";

    private static Connection conexion;

    private Conexion() {
        // Constructor privado (patrón Singleton)
    }

    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conexion;
    }
}

