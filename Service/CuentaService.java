package Service;

import DAO.CuentaDAO;
import modelo.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public class CuentaService {

    private final CuentaDAO cuentaDAO = new CuentaDAO();

    public void crearCuenta(int idCliente, Cuenta.Tipo tipo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setTipo(tipo);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstado(Cuenta.Estado.ACTIVA);
        cuenta.setIdCliente(idCliente);

        cuentaDAO.crearCuenta(cuenta);
    }

    public List<Cuenta> obtenerCuentasCliente(int idCliente) {
        return cuentaDAO.listarPorCliente(idCliente);
    }

    public void cerrarCuenta(int idCuenta) {
        cuentaDAO.cerrarCuenta(idCuenta);
    }

    private String generarNumeroCuenta() {
        return "AC" + System.currentTimeMillis();
    }
}
