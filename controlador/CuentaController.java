package controlador;

import Service.CuentaService;
import modelo.Cuenta;

public class CuentaController {

    private final CuentaService cuentaService = new CuentaService();

    public void crearCuenta(int idCliente, Cuenta.Tipo tipo) {
        cuentaService.crearCuenta(idCliente, tipo);
    }

    public void cerrarCuenta(int idCuenta) {
        cuentaService.cerrarCuenta(idCuenta);
    }
}

