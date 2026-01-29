package proyecto.simulador.bancario.Service;

import proyecto.simulador.bancario.DAO.ClienteDAO;
import proyecto.simulador.bancario.modelo.Cliente;

public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public void crearCliente(String nombre, String cedula, String email,
                             String telefono, String direccion,
                             Cliente.Estado estado, int idUsuario) {

        // Validaciones de negocio
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre obligatorio");
        else if (cedula == null || cedula.isBlank())
            throw new IllegalArgumentException("Cédula obligatoria");
        else if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email obligatorio");
        else if (telefono == null || telefono.isBlank())
            throw new IllegalArgumentException("Teléfono obligatorio");
        else if (direccion == null || direccion.isBlank())
            throw new IllegalArgumentException("Dirección obligatoria");

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setCedula(cedula);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setEstado(estado);
        cliente.setIdUsuario(idUsuario);

        clienteDAO.crearCliente(cliente);
    }

    public void cambiarEstado(int idCliente, Cliente.Estado estado) {
        clienteDAO.actualizarEstado(idCliente, estado);
    }
}

