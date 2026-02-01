package proyecto.simulador.bancario.Service;

import java.time.LocalDate;
import java.time.Period;

import proyecto.simulador.bancario.DAO.ClienteDAO;
import proyecto.simulador.bancario.modelo.Cliente;

public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public void crearCliente(String pNombre, String sNombre, String pApellido, String sApellido,
                             String sexo, LocalDate fechaNac, String cedula, String email,
                             String telefono, String direccion,
                             Cliente.Estado estado, int idUsuario) {

        // Validaciones de nulidad y vacío
        if (estaVacio(pNombre) || estaVacio(pApellido) || estaVacio(cedula) || estaVacio(email)) {
            throw new IllegalArgumentException("Los campos principales son obligatorios.");
        }

         // -------- VALIDACIÓN DE TELÉFONO --------
        if (!estaVacio(telefono) && !telefono.matches("\\d{7,10}")) {
            throw new IllegalArgumentException("El teléfono debe contener solo números (7 a 10 dígitos).");
        }

        // -------- VALIDACIÓN DE CÉDULA --------
        if (!cedula.matches("\\d{10}")) {
            throw new IllegalArgumentException("La cédula debe contener exactamente 10 números.");
        }
       // --- VALIDACIONES DE FECHA ---
        if (fechaNac == null) throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        LocalDate hoy = LocalDate.now();
        if (fechaNac.isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser una fecha futura.");
        }
        // Calcular edad
        int edad = Period.between(fechaNac, hoy).getYears();
        // Validación de mayoría de edad
        if (edad < 18) {
            throw new IllegalArgumentException("Debe ser mayor de 18 años (Edad: " + edad + ").");
        }
        // Validación de edad máxima razonable
        if (edad > 130) {
            throw new IllegalArgumentException("La edad no puede superar los 130 años (Edad: " + edad + ").");
        }

        Cliente cliente = new Cliente();
        cliente.setPrimerNombre(pNombre);
        cliente.setSegundoNombre(sNombre);
        cliente.setPrimerApellido(pApellido);
        cliente.setSegundoApellido(sApellido);
        cliente.setSexo(sexo);
        cliente.setFechaNacimiento(fechaNac);
        cliente.setCedula(cedula);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setEstado(estado);
        cliente.setIdUsuario(idUsuario);
        clienteDAO.crearCliente(cliente);

    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }



    public void cambiarEstado(int idCliente, Cliente.Estado estado) {
        clienteDAO.actualizarEstado(idCliente, estado);
    }

    public Cliente obtenerClientePorUsuario(int idUsuario) {
        return clienteDAO.buscarPorUsuario(idUsuario);
    }
}
