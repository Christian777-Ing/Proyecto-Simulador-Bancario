package proyecto.simulador.bancario;

import java.sql.Connection;
import java.sql.SQLException;

import proyecto.simulador.bancario.Data_Base.Conexion;

public class MainPrueba {
    public static void main(String[] args) {
    try (Connection cn = Conexion.getConexion()) {
        System.out.println("Conectado OK");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}
