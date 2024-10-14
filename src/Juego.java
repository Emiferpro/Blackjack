import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Juego extends JFrame {
    private String nombreJugador;
    private Jugador jugador;
    private Jugador dealer; // Usar Jugador como dealer
    private Mazo mazo;
    private final JButton btnPedirCarta;
    private final JButton btnPlantarse;
    private final JButton btnVolverAJugar;
    private final JPanel panelCartasDealer;
    private final JPanel panelCartas;
    private final JLabel lblPuntosJugador;
    private final JLabel lblPuntosDealer;
    private boolean mostrarTodasCartasDealer = false; // Para controlar la visualización de las cartas del dealer
    private Clip clip;

    public Juego() {
        setTitle("Blackjack");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel para mostrar cartas del dealer
        panelCartasDealer = new JPanel();
        panelCartasDealer.setLayout(new FlowLayout());  // Puedes cambiar el layout si lo deseas
        panelCartasDealer.setBorder(BorderFactory.createTitledBorder("Dealer")); // Añadir borde con título
        add(panelCartasDealer, BorderLayout.NORTH); // Agregar el panel del dealer en la parte superior

        // Panel para mostrar cartas del jugador
        panelCartas = new JPanel();
        panelCartas.setLayout(new FlowLayout());  // Layout para las cartas del jugador
        panelCartas.setBorder(BorderFactory.createTitledBorder("Jugador")); // Añadir borde con título
        add(panelCartas, BorderLayout.CENTER); // Agregar el panel del jugador en el centro
        // Panel de botones
        JPanel panelBotones = new JPanel();
        btnPedirCarta = new JButton("Pedir Carta");
        btnPlantarse = new JButton("Plantarse");
        btnVolverAJugar = new JButton("Volver a Jugar");
        btnVolverAJugar.setEnabled(false); // Desactivar al inicio

        panelBotones.add(btnPedirCarta);
        panelBotones.add(btnPlantarse);
        panelBotones.add(btnVolverAJugar);
        add(panelBotones, BorderLayout.SOUTH);

        // Etiquetas para mostrar puntos
        lblPuntosJugador = new JLabel("Puntos Jugador: 0");
        lblPuntosDealer = new JLabel("Puntos Dealer: 0");
        panelBotones.add(lblPuntosJugador);
        panelBotones.add(lblPuntosDealer);

        // Eventos de botones
        btnPedirCarta.addActionListener(_ -> pedirCarta());
        btnPlantarse.addActionListener(_ -> plantarse());
        btnVolverAJugar.addActionListener(_ -> reiniciarJuego());

        // Inicializa el juego
        mostrarMenuInicio();
    }

    private void mostrarMenuInicio() {
        if (jugador == null) {
            nombreJugador = JOptionPane.showInputDialog("Introduce tu nombre:");
        }
        if (nombreJugador != null && !nombreJugador.isEmpty()) {
            jugador = new Jugador(nombreJugador);
            panelCartas.setBorder(BorderFactory.createTitledBorder(nombreJugador));
            dealer = new Jugador("Dealer"); // Inicializa al dealer
            iniciarJuego();
        } else {
            System.exit(0); // Salir si no se proporciona un nombre
        }
    }

    private void iniciarJuego() {
        mazo = new Mazo();

        // Reparte dos cartas al jugador y al dealer
        jugador.agregarCarta(mazo.sacarCarta());
        jugador.agregarCarta(mazo.sacarCarta());
        dealer.agregarCarta(mazo.sacarCarta());
        dealer.agregarCarta(mazo.sacarCarta());

        mostrarCartas(); // Muestra las cartas del jugador
        mostrarCartasDealer(); // Muestra solo una carta del dealer
        lblPuntosJugador.setText("Puntos Jugador: " + jugador.totalMano());
        lblPuntosDealer.setText("Puntos Dealer: " + (mostrarTodasCartasDealer ? dealer.totalMano() : "??"));

        // Iniciar música de fondo
        reproducirMusica();
    }

    private void mostrarCartas() {
        panelCartas.removeAll(); // Limpia el panel antes de agregar nuevas cartas
        for (Carta carta : jugador.getMano()) {
            JLabel cartaLabel = crearEtiquetaCarta(carta);
            panelCartas.add(cartaLabel);
        }
        panelCartas.revalidate(); // Vuelve a validar el panel
        panelCartas.repaint(); // Redibuja el panel
        lblPuntosJugador.setText("Puntos Jugador: " + jugador.totalMano()); // Actualizar puntos
    }

    private void mostrarCartasDealer() {
        panelCartasDealer.removeAll(); // Limpia el panel antes de agregar nuevas cartas
        if (mostrarTodasCartasDealer) {
            for (Carta carta : dealer.getMano()) {
                JLabel cartaLabel = crearEtiquetaCarta(carta);
                panelCartasDealer.add(cartaLabel);
            }
        } else {
            // Muestra todas menos la primer carta del dealer
            for (Carta carta : dealer.getMano()) {
                if (carta != dealer.getMano().getFirst()) {
                    JLabel cartaLabel = crearEtiquetaCarta(carta);
                    panelCartasDealer.add(cartaLabel);
                } else {
                    JLabel cartaLabel = crearEtiquetaCarta(Carta.getEmpty());
                    panelCartasDealer.add(cartaLabel);
                }
            }
        }
        panelCartasDealer.revalidate(); // Vuelve a validar el panel
        panelCartasDealer.repaint(); // Redibuja el panel
        lblPuntosDealer.setText("Puntos Dealer: " + (mostrarTodasCartasDealer ? dealer.totalMano() : "??")); // Actualizar puntos
    }

    private JLabel crearEtiquetaCarta(Carta carta) {
        JLabel cartaLabel = new JLabel();
        try {
            // Carga la imagen de la carta
            String valor = switch (carta.getValor()) {
                case "A" -> "ace";
                case "K" -> "king";
                case "Q" -> "queen";
                case "J" -> "jack";
                case "null" -> "backside";
                default -> carta.getValor();
            };
            String palo = switch (carta.getPalo()) {
                case "Tréboles" -> "clubs";
                case "Diamantes" -> "diamonds";
                case "Corazones" -> "hearts";
                case "Picas" -> "spades";
                case "null" -> "backside";
                default -> throw new IllegalStateException("Unexpected value: " + carta.getPalo());
            };
            BufferedImage imagen = ImageIO.read(new File("cards/" + valor + "_of_" + palo + ".png"));
            ImageIcon icono = new ImageIcon(imagen.getScaledInstance(100, 150, Image.SCALE_SMOOTH)); // Escala la imagen
            cartaLabel.setIcon(icono);
        } catch (Exception e) {
            cartaLabel.setText(carta.toString()); // Si no se puede cargar la imagen, muestra el texto
        }
        return cartaLabel;
    }

    private void pedirCarta() {
        if (mazo.hayCartas()) {
            jugador.agregarCarta(mazo.sacarCarta());
            mostrarCartas();
            if (jugador.totalMano() > 21) {
                JOptionPane.showMessageDialog(this, "¡Te pasaste! Perdiste :(");
                mostrarTodasCartasDealer = true; // Revelar todas las cartas del dealer
                mostrarCartasDealer(); // Mostrar todas las cartas del dealer
                btnPedirCarta.setEnabled(false);
                btnPlantarse.setEnabled(false);
                btnVolverAJugar.setEnabled(true);
                detenerMusica();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No quedan cartas en el mazo.");
        }
    }

    private void plantarse() {
        JOptionPane.showMessageDialog(this, jugador.getNombre() + " se ha plantado con un total de: " + jugador.totalMano());
        jugarDealer(); // Llama al dealer para que tome su turno
        mostrarTodasCartasDealer = true; // Revelar todas las cartas del dealer
        mostrarCartasDealer(); // Mostrar todas las cartas del dealer
        btnPedirCarta.setEnabled(false);
        btnPlantarse.setEnabled(false);
        btnVolverAJugar.setEnabled(true);
        detenerMusica();
    }

    private void jugarDealer() {
        while (true) {
            int puntosDealer = dealer.totalMano();
            int cartasRestantes = mazo.cartasRestantes();
            double probabilidadDePasarse = calcularProbabilidadDePasarse(puntosDealer, cartasRestantes);

            if (puntosDealer >= 17 && probabilidadDePasarse >= 0.5) {
                JOptionPane.showMessageDialog(this ,"Dealer se planta con " + puntosDealer + " puntos.");
                break;
            } else if (puntosDealer < 17 || probabilidadDePasarse < 0.5) {
                dealer.agregarCarta(mazo.sacarCarta());
                mostrarCartasDealer(); // Redibujar cada que el dealer juegue
                JOptionPane.showMessageDialog(this, "Dealer pide una carta. Ahora tiene: " + dealer.totalMano() + " puntos.");
                lblPuntosDealer.setText("Puntos Dealer: " + dealer.totalMano()); // Actualizar puntos

                if (dealer.totalMano() > 21) {
                    JOptionPane.showMessageDialog(this,"Dealer se ha pasado con " + dealer.totalMano() + " puntos.");
                    break;
                }
            }
        }
        determinarGanador(); // Determinar ganador al final del turno del dealer
    }

    private void determinarGanador() {
        int puntosJugador = jugador.totalMano();
        int puntosDealer = dealer.totalMano();

        if (puntosJugador > 21) {
            JOptionPane.showMessageDialog(this, "¡Te has pasado! Has perdido.");
            mostrarTodasCartasDealer = true;
            mostrarCartasDealer();
        } else if (puntosDealer > 21 || puntosJugador > puntosDealer) {
            JOptionPane.showMessageDialog(this, "¡Felicidades! Ganaste con " + puntosJugador + " puntos.");
            mostrarTodasCartasDealer = true;
            mostrarCartasDealer();
        } else if (puntosJugador < puntosDealer) {
            JOptionPane.showMessageDialog(this, "El dealer gana con " + puntosDealer + " puntos.");
            mostrarTodasCartasDealer = true;
            mostrarCartasDealer();
        } else {
            JOptionPane.showMessageDialog(this, "¡Es un empate! Ambos tienen " + puntosJugador + " puntos.");
            mostrarTodasCartasDealer = true;
            mostrarCartasDealer();
        }
    }

    private double calcularProbabilidadDePasarse(int puntosDealer, int cartasRestantes) {
        if (cartasRestantes == 0) return 0.0; // Evita división por cero

        // Cartas que podrían hacer que el dealer se pase
        int cartasQueHacenPasarse = 0;

        // Contamos las cartas que harán que el dealer se pase
        for (Carta carta : mazo.getCartasRestantes()) {
            if (puntosDealer + carta.valorEnPuntos() > 21) {
                cartasQueHacenPasarse++;
            }
        }

        return (double) cartasQueHacenPasarse / cartasRestantes; // Retorna probabilidad
    }


    private void reiniciarJuego() {
        String[] options = new String[] {"Si", "No", "Salir"};
        int response = JOptionPane.showOptionDialog(null, "¿Desea seguir jugando con el mismo usuario?", "BlackJack",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if (response == 1) {
            jugador = null;
        }
        if (response == 2) {
            System.exit(0);
        }
        dealer = null;
        panelCartas.removeAll();
        mostrarTodasCartasDealer = false;
        lblPuntosJugador.setText("Puntos Jugador: 0");
        lblPuntosDealer.setText("Puntos Dealer: 0");
        btnPedirCarta.setEnabled(true); // Habilitar botón "Pedir Carta"
        btnPlantarse.setEnabled(true); // Habilitar botón "Plantarse"
        btnVolverAJugar.setEnabled(false);
        mostrarMenuInicio();
        detenerMusica();
    }


    private void reproducirMusica() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("sound.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce en bucle
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void detenerMusica() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Juego juego = new Juego();
            juego.setVisible(true);
        });
    }
}
