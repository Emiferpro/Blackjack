import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Juego extends JFrame {
    private Jugador jugador;
    private Jugador dealer; // Usar Jugador como dealer
    private Mazo mazo;
    private final JTextArea areaTexto;
    private final JButton btnPedirCarta;
    private final JButton btnPlantarse;
    private final JButton btnVolverAJugar;
    private final JPanel panelCartas;
    private final JLabel lblPuntosJugador;
    private final JLabel lblPuntosDealer;
    private boolean mostrarTodasCartasDealer = false; // Para controlar la visualización de las cartas del dealer
    private Clip clip;

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
        String nombreJugador = JOptionPane.showInputDialog("Introduce tu nombre:");
        if (nombreJugador != null && !nombreJugador.isEmpty()) {
            jugador = new Jugador(nombreJugador);
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
        areaTexto.setText(jugador.toString() + "\n");
        lblPuntosJugador.setText("Puntos Jugador: " + jugador.totalMano());
        lblPuntosDealer.setText("Puntos Dealer: " + dealer.totalMano());

        // Iniciar música de fondo
        reproducirMusica("sound.wav");
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
        panelCartas.removeAll(); // Limpia el panel antes de agregar nuevas cartas
        if (mostrarTodasCartasDealer) {
            for (Carta carta : dealer.getMano()) {
                JLabel cartaLabel = crearEtiquetaCarta(carta);
                panelCartas.add(cartaLabel);
            }
        } else {
            // Muestra solo la primera carta del dealer
            JLabel cartaLabel = crearEtiquetaCarta(dealer.getMano().get(0)); // Muestra solo la primera carta
            panelCartas.add(cartaLabel);
            panelCartas.add(new JLabel("Carta oculta")); // Muestra una etiqueta para la carta oculta
        }
        panelCartas.revalidate(); // Vuelve a validar el panel
        panelCartas.repaint(); // Redibuja el panel
        lblPuntosDealer.setText("Puntos Dealer: " + dealer.totalMano()); // Actualizar puntos
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
        return cartaLabel;
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
                btnVolverAJugar.setEnabled(true);
                detenerMusica();
            }
        } else {
            areaTexto.append("No quedan cartas en el mazo.\n");
        }
    }

    private void plantarse() {
        areaTexto.append(jugador.getNombre() + " se ha plantado con un total de: " + jugador.totalMano() + "\n");
        mostrarTodasCartasDealer = true; // Revelar todas las cartas del dealer
        mostrarCartasDealer(); // Mostrar todas las cartas del dealer
        jugarDealer(); // Llama al dealer para que tome su turno
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
                areaTexto.append("Dealer se planta con " + puntosDealer + " puntos.\n");
                break;
            } else if (puntosDealer < 17 || probabilidadDePasarse < 0.5) {
                dealer.agregarCarta(mazo.sacarCarta());
                areaTexto.append("Dealer pide una carta. Ahora tiene: " + dealer.totalMano() + " puntos.\n");
                lblPuntosDealer.setText("Puntos Dealer: " + dealer.totalMano()); // Actualizar puntos

                if (dealer.totalMano() > 21) {
                    areaTexto.append("Dealer se ha pasado con " + dealer.totalMano() + " puntos.\n");
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
            areaTexto.append("¡Te has pasado! Has perdido.\n");
        } else if (puntosDealer > 21 || puntosJugador > puntosDealer) {
            areaTexto.append("¡Felicidades! Ganaste con " + puntosJugador + " puntos.\n");
        } else if (puntosJugador < puntosDealer) {
            areaTexto.append("El dealer gana con " + puntosDealer + " puntos.\n");
        } else {
            areaTexto.append("¡Es un empate! Ambos tienen " + puntosJugador + " puntos.\n");
        }
    }

    private double calcularProbabilidadDePasarse(int puntosDealer, int cartasRestantes) {
        if (cartasRestantes == 0) return 0.0; // Evita división por cero

        int cartasQueHacenPasarse = 0;
        for (int i = 1; i <= 10; i++) {
            if (puntosDealer + i > 21) {
                cartasQueHacenPasarse++;
            }
        }
        return (double) cartasQueHacenPasarse / cartasRestantes; // Retorna probabilidad
    }

    private void reiniciarJuego() {
        jugador = null;
        dealer = null;
        panelCartas.removeAll();
        areaTexto.setText("");
        lblPuntosJugador.setText("Puntos Jugador: 0");
        lblPuntosDealer.setText("Puntos Dealer: 0");
        btnPedirCarta.setEnabled(true); // Habilitar botón "Pedir Carta"
        btnPlantarse.setEnabled(true); // Habilitar botón "Plantarse"
        btnVolverAJugar.setEnabled(false);
        mostrarMenuInicio();
        detenerMusica();
    }


    private void reproducirMusica(String ruta) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(ruta));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce en bucle
        } catch (Exception e) {
            e.printStackTrace();
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
