package proyecto.simulador.bancario.modelo;

public class Usuario {

    private int idUsuario;
    private String username;
    private String passwordHash;
    private Rol rol;

    public enum Rol {
        ADMIN, CLIENTE
    }

    public Usuario() {}

    public Usuario(int idUsuario, String username, String passwordHash, Rol rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}