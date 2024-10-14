import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nombre;

    public Cliente(String nombre, String ipServidor, int puerto) {
        this.nombre = nombre;
        try {
            // Conectar con el servidor
            socket = new Socket(ipServidor, puerto);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Enviar nombre del jugador al servidor
            output.println(nombre);
        } catch (IOException e) {
            System.err.println("Error al conectarse con el servidor: " + e.getMessage());
        }
    }

    // Enviar mensaje al servidor
    public void enviarMensaje(String mensaje) {
        output.println(mensaje);
    }

    // Recibir mensaje del servidor
    public String recibirMensaje() throws IOException {
        return input.readLine();
    }

    // Lógica para manejar las interacciones con el servidor
    public void jugar() {
        try {
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            String respuestaServidor;

            while ((respuestaServidor = recibirMensaje()) != null) {
                System.out.println(respuestaServidor); // Mostrar mensaje del servidor

                // Condiciones para responder al servidor
                if (respuestaServidor.contains("Tu turno")) {
                    System.out.println("Elige una acción: [pedir] / [plantarse]");
                    String accion = teclado.readLine();
                    enviarMensaje(accion); // Enviar la acción elegida al servidor
                }

                if (respuestaServidor.contains("¿Quieres jugar de nuevo?")) {
                    System.out.println("Escribe [si] o [no]");
                    String jugarDeNuevo = teclado.readLine();
                    enviarMensaje(jugarDeNuevo); // Enviar la decisión al servidor
                }
            }

        } catch (IOException e) {
            System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    // Cerrar la conexión con el servidor
    public void cerrarConexion() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}