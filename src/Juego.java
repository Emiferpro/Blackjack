import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Juego extends JFrame {
    private Jugador jugador;
    private Mazo mazo;
    private final JTextArea areaTexto;
    private final JButton btnPedirCarta;
    private final JButton btnPlantarse;
    private final JPanel panelCartas;

    public Juego() {
        setTitle("Blackjack");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Área de texto para mostrar el estado del juego
        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.NORTH);

        // Panel para mostrar cartas
        panelCartas = new JPanel();
        panelCartas.setLayout(new FlowLayout());
        add(panelCartas, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        btnPedirCarta = new JButton("Pedir Carta");
        btnPlantarse = new JButton("Plantarse");

        panelBotones.add(btnPedirCarta);
        panelBotones.add(btnPlantarse);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnPedirCarta.addActionListener(_ -> pedirCarta());

        btnPlantarse.addActionListener(_ -> plantarse());

        // Inicializa el juego
        iniciarJuego();
    }

    private void iniciarJuego() {
        String nombreJugador = JOptionPane.showInputDialog("Introduce tu nombre:");
        jugador = new Jugador(nombreJugador);
        mazo = new Mazo();

        // Reparte dos cartas al jugador
        jugador.agregarCarta(mazo.sacarCarta());
        jugador.agregarCarta(mazo.sacarCarta());
        mostrarCartas();
        areaTexto.setText(jugador.toString() + "\n");
    }

    private void mostrarCartas() {
        panelCartas.removeAll(); // Limpia el panel antes de agregar nuevas cartas
        for (Carta carta : jugador.getMano()) {
            JLabel cartaLabel = new JLabel();
            try {
                // Carga la imagen de la carta
                String valor = switch (carta.getValor()) {
                    case "A" -> "ace";
                    case "K" -> "king";
                    case "Q" -> "queen";
                    case "J" -> "jack";
                    default -> carta.getValor();
                };
                String palo = switch (carta.getPalo()) {
                    case "Tréboles" -> "clubs";
                    case "Diamantes" -> "diamonds";
                    case "Corazones" -> "hearts";
                    case "Picas" -> "spades";
                    default -> throw new IllegalStateException("Unexpected value: " + carta.getPalo());
                };
                BufferedImage imagen = ImageIO.read(new File("cards/" + valor + "_of_" + palo + ".png"));
                ImageIcon icono = new ImageIcon(imagen.getScaledInstance(100, 150, Image.SCALE_SMOOTH)); // Escala la imagen
                cartaLabel.setIcon(icono);
            } catch (Exception e) {
                cartaLabel.setText(carta.toString()); // Si no se puede cargar la imagen, muestra el texto
            }
            panelCartas.add(cartaLabel);
        }
        panelCartas.revalidate(); // Vuelve a validar el panel
        panelCartas.repaint(); // Redibuja el panel
    }

    private void pedirCarta() {
        if (mazo.hayCartas()) {
            jugador.agregarCarta(mazo.sacarCarta());
            mostrarCartas();
            areaTexto.append(jugador.toString() + "\n");
            if (jugador.totalMano() > 21) {
                areaTexto.append("¡Te has pasado! Has perdido.\n");
                btnPedirCarta.setEnabled(false);
                btnPlantarse.setEnabled(false);
            }
        } else {
            areaTexto.append("No quedan cartas en el mazo.\n");
        }
    }

    private void plantarse() {
        areaTexto.append(jugador.getNombre() + " se ha plantado con un total de: " + jugador.totalMano() + "\n");
        btnPedirCarta.setEnabled(false);
        btnPlantarse.setEnabled(false);
        // TODO: Agregar dealer
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Juego juego = new Juego();
            juego.setVisible(true);
        });
    }
}
