package proyecto.simulador.bancario.controlador;

import proyecto.simulador.bancario.modelo.Cliente;
import proyecto.simulador.bancario.Service.ClienteService;

public class ClienteController {

    private final ClienteService clienteService = new ClienteService();

    public void crearCliente(String nombre, String cedula, String email,
                             String telefono, String direccion, Cliente.Estado estado, int idUsuario) {

        clienteService.crearCliente(
            nombre, cedula, email, telefono, direccion, estado, idUsuario
        );
    }

    public void activarCliente(int idCliente) {
        clienteService.cambiarEstado(idCliente, Cliente.Estado.ACTIVO);
    }

    public void bloquearCliente(int idCliente) {
        clienteService.cambiarEstado(idCliente, Cliente.Estado.BLOQUEADO);
    }
}
