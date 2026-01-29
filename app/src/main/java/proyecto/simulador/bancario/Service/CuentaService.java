package proyecto.simulador.bancario.Service;

import proyecto.simulador.bancario.DAO.CuentaDAO;
import proyecto.simulador.bancario.DAO.TransferenciaDAO;
import proyecto.simulador.bancario.Data_Base.Conexion;
import proyecto.simulador.bancario.modelo.Cuenta;
import proyecto.simulador.bancario.modelo.Transaccion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class CuentaService {

    private final CuentaDAO cuentaDAO = new CuentaDAO();
    private final TransferenciaDAO transferenciaDAO = new TransferenciaDAO();

    // Crear cuenta
    public void crearCuenta(int idCliente, Cuenta.Tipo tipo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setTipo(tipo);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstado(Cuenta.Estado.ACTIVA);
        cuenta.setIdCliente(idCliente);

        cuentaDAO.crearCuenta(cuenta);
    }

    // Listar cuentas de un cliente
    public List<Cuenta> obtenerCuentasCliente(int idCliente) {
        return cuentaDAO.listarPorCliente(idCliente);
    }

    // Cerrar cuenta
    public void cerrarCuenta(int idCuenta) {
        cuentaDAO.cerrarCuenta(idCuenta);
    }

    // Depósito
    public void depositar(int idCuenta, BigDecimal monto) throws Exception {
        Cuenta cuenta = cuentaDAO.buscarPorId(idCuenta);
        validarCuentaActiva(cuenta);

        cuentaDAO.actualizarSaldo(idCuenta, cuenta.getSaldo().add(monto));

        Transaccion t = new Transaccion();
        t.setTipo(Transaccion.Tipo.DEPOSITO);
        t.setMonto(monto);
        t.setFecha(LocalDateTime.now());
        t.setIdCuentaOrigen(idCuenta);
        t.setIdCuentaDestino(null);
        transferenciaDAO.crearTransferencia(t);
    }

    // Retiro
    public void retirar(int idCuenta, BigDecimal monto) throws Exception {
        Cuenta cuenta = cuentaDAO.buscarPorId(idCuenta);
        validarCuentaActiva(cuenta);

        if (cuenta.getSaldo().compareTo(monto) < 0)
            throw new IllegalStateException("Saldo insuficiente");

        cuentaDAO.actualizarSaldo(idCuenta, cuenta.getSaldo().subtract(monto));

        Transaccion transaccion = new Transaccion();
        transaccion.setTipo(Transaccion.Tipo.RETIRO);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setIdCuentaOrigen(idCuenta);
        transaccion.setIdCuentaDestino(null);
        transferenciaDAO.crearTransferencia(transaccion);
    }

    // Transferencia
    public void transferir(int idCuentaOrigen, int idCuentaDestino, BigDecimal monto) throws Exception {
        Connection conn = Conexion.getConexion();
        try {
            conn.setAutoCommit(false);

            Cuenta origen = cuentaDAO.buscarPorId(idCuentaOrigen);
            Cuenta destino = cuentaDAO.buscarPorId(idCuentaDestino);

            validarCuentaActiva(origen);
            validarCuentaActiva(destino);

            if (origen.getSaldo().compareTo(monto) < 0)
                throw new IllegalStateException("Saldo insuficiente");

            // Restar del origen
            cuentaDAO.actualizarSaldo(idCuentaOrigen, origen.getSaldo().subtract(monto));
            // Sumar al destino
            cuentaDAO.actualizarSaldo(idCuentaDestino, destino.getSaldo().add(monto));

            // Registrar transacción
            Transaccion transaccion = new Transaccion();
            transaccion.setTipo(Transaccion.Tipo.TRANSFERENCIA);
            transaccion.setMonto(monto);
            transaccion.setFecha(LocalDateTime.now());
            transaccion.setIdCuentaOrigen(idCuentaOrigen);
            transaccion.setIdCuentaDestino(idCuentaDestino);
            transferenciaDAO.crearTransferencia(transaccion);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Validación de cuenta activa
    private void validarCuentaActiva(Cuenta cuenta) {
        if (cuenta == null)
            throw new IllegalStateException("Cuenta no existe");
        if (cuenta.getEstado() != Cuenta.Estado.ACTIVA)
            throw new IllegalStateException("Cuenta bloqueada o cerrada");
    }

    // Generar número de cuenta
    private String generarNumeroCuenta() {
        return "AC" + System.currentTimeMillis();
    }

    // Ver saldo de una cuenta
    public BigDecimal verSaldo(int idCuenta) throws Exception {
        Cuenta cuenta = cuentaDAO.buscarPorId(idCuenta);
        validarCuentaActiva(cuenta);
        return cuenta.getSaldo();
    }

    // Ver historial de transacciones de una cuenta
    public List<Transaccion> verHistorial(int idCuenta) {
        return transferenciaDAO.listarPorCuenta(idCuenta);
    }

    // Filtrar historial por tipo
    public List<Transaccion> filtrarPorTipo(int idCuenta, Transaccion.Tipo tipo) {
        return verHistorial(idCuenta)
                .stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
    }

    // Filtrar historial por rango de fechas
    public List<Transaccion> filtrarPorFecha(int idCuenta, LocalDate inicio, LocalDate fin) {
        LocalDateTime start = inicio.atStartOfDay();
        LocalDateTime end = fin.atTime(LocalTime.MAX);
        return verHistorial(idCuenta)
                .stream()
                .filter(t -> !t.getFecha().isBefore(start) && !t.getFecha().isAfter(end))
                .collect(Collectors.toList());
    }
}
