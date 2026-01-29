package proyecto.simulador.bancario.controlador;

import proyecto.simulador.bancario.modelo.Cliente;

import java.time.LocalDate;

import proyecto.simulador.bancario.Service.ClienteService;

public class ClienteController {

    private final ClienteService clienteService = new ClienteService();

    public void crearCliente(String pNombre, String sNombre, String pApellido, String sApellido,
                             String sexo, LocalDate fechaNac, String cedula, String email,
                             String telefono, String direccion, Cliente.Estado estado, int idUsuario) {

        // Llama al servicio que contiene la validación de 18 años
        clienteService.crearCliente(
            pNombre, sNombre, pApellido, sApellido, sexo, 
            fechaNac, cedula, email, telefono, direccion, estado, idUsuario
        );
    }

    public void activarCliente(int idCliente) {
        clienteService.cambiarEstado(idCliente, Cliente.Estado.ACTIVO);
    }

    public void bloquearCliente(int idCliente) {
        clienteService.cambiarEstado(idCliente, Cliente.Estado.BLOQUEADO);
    }
}
