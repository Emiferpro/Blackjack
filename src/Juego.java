import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
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
    private final JButton btnFold;
    private final JButton btnDouble;
    private final JButton btnSeguro;
    private final JPanel panelCartasDealer;
    private final JPanel panelCartas;
    private final JLabel lblPuntosJugador;
    private final JLabel lblPuntosDealer;
    private final JLabel lblMoneyJugador;
    private final JLabel lblMoneyDealer;
    private boolean mostrarTodasCartasDealer = false; // Para controlar la visualización de las cartas del dealer
    private boolean habilitarSeguro;
    private Clip clip;

    public Juego() {
        setTitle("Blackjack");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel para mostrar cartas del dealer
        panelCartasDealer = new JPanel();
        panelCartasDealer.setLayout(new FlowLayout());
        panelCartasDealer.setBorder(BorderFactory.createTitledBorder("Dealer")); // Añadir borde con título
        add(panelCartasDealer, BorderLayout.NORTH); // Agregar el panel del dealer en la parte superior

        // Panel para mostrar cartas del jugador
        panelCartas = new JPanel();
        panelCartas.setLayout(new FlowLayout());  // Layout para las cartas del jugador
        panelCartas.setBorder(BorderFactory.createTitledBorder("Jugador")); // Añadir borde con título
        add(panelCartas, BorderLayout.CENTER); // Agregar el panel del jugador en el centro
        // Panel de botones
        JPanel combinedPanel = new JPanel();
        btnPedirCarta = new JButton("Pedir Carta");
        btnPlantarse = new JButton("Plantarse");
        btnVolverAJugar = new JButton("Volver a Jugar");
        btnFold = new JButton("Fold");
        btnDouble = new JButton("Duplicar");
        btnSeguro = new JButton("Seguro");
        btnVolverAJugar.setEnabled(false); // Desactivar al inicio
        lblPuntosJugador = new JLabel(" Puntos Jugador: 0");
        lblPuntosDealer = new JLabel(" Puntos Dealer: 0");
        lblMoneyJugador = new JLabel(" Dinero del Jugador: 0");
        lblMoneyDealer = new JLabel(" Dinero del Dealer: 0");
        combinedPanel.setLayout(new GridLayout(2, 7)); // 2 rows, 3 columns
        combinedPanel.add(btnPedirCarta);
        combinedPanel.add(btnPlantarse);
        combinedPanel.add(btnVolverAJugar);
        combinedPanel.add(lblPuntosJugador);
        combinedPanel.add(lblPuntosDealer);
        combinedPanel.add(btnFold);
        combinedPanel.add(btnDouble);
        combinedPanel.add(btnSeguro);
        combinedPanel.add(lblMoneyJugador);
        combinedPanel.add(lblMoneyDealer);
        add(combinedPanel, BorderLayout.SOUTH);


        // Eventos de botones
        btnPedirCarta.addActionListener(_ -> pedirCarta());
        btnPlantarse.addActionListener(_ -> plantarse());
        btnVolverAJugar.addActionListener(_ -> reiniciarJuego());
        btnDouble.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "No implementado");
        });
        btnSeguro.addActionListener(_ -> seguro());
        btnFold.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "No implementado");
        });

        // Inicializa el juego
        mostrarMenuInicio();
    }

    private void mostrarMenuInicio() {
        if (jugador == null) {
            nombreJugador = JOptionPane.showInputDialog("Introduce tu nombre:");
        }
        if (nombreJugador != null && !nombreJugador.isEmpty()) {
            jugador = new Jugador(nombreJugador, jugador != null ? jugador.getDinero() : 100);
            panelCartas.setBorder(BorderFactory.createTitledBorder(nombreJugador));
            dealer = new Jugador("Dealer"); // Inicializa al dealer
            iniciarJuego();
        } else {
            System.exit(0); // Salir si no se proporciona un nombre
        }
    }

    private void iniciarJuego() {
        mazo = new Mazo();
        habilitarSeguro = true;
        // Reparte dos cartas al jugador y al dealer
        jugador.agregarCarta(mazo.sacarCarta());
        jugador.agregarCarta(mazo.sacarCarta());
        dealer.agregarCarta(mazo.sacarCarta());
        dealer.agregarCarta(mazo.sacarCarta());
        Random rn = new Random();
        dealer.setApuesta((int) (dealer.getDinero() * rn.nextDouble(0.5)));
        boolean betSet = false;
        while (!betSet) {
            try {
                int apuesta = Integer.parseInt(JOptionPane.showInputDialog("Ingresa tu apuesta"));
                if (jugador.getDinero() <= 0) {
                    JOptionPane.showMessageDialog(this, "Sin plata no vas a llegar muy lejos");
                    gameOver();
                } else if (apuesta <= jugador.getDinero()) {
                    jugador.setApuesta(apuesta);
                    betSet = true;
                } else if (apuesta == 0) {
                    JOptionPane.showMessageDialog(this, "Si no apuestas no hay diversión");
                } else {
                    JOptionPane.showMessageDialog(this, "Vuelve a intentarlo con un valor menor, ¿o quieres endeudarte?");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Tienes que ingresar un numero. ¿O quieres apostar tu casa?");
            }
        }
        mostrarCartas(); // Muestra las cartas del jugador
        mostrarCartasDealer(); // Muestra solo una carta del dealer
        lblPuntosJugador.setText(" Puntos Jugador: " + jugador.totalMano());
        lblPuntosDealer.setText(" Puntos Dealer: " + (mostrarTodasCartasDealer ? dealer.totalMano() : "??"));
        lblMoneyDealer.setText(" Dinero del Dealer: " + dealer.getDinero());
        lblMoneyJugador.setText(" Dinero del Jugador: " + jugador.getDinero());
        // Iniciar música de fondo
        reproducirMusica();
        refreshBets();
        if (jugador.totalMano() == 21) {
            JOptionPane.showMessageDialog(this, jugador.getNombre() + " ha ganado por BlackJack");
            jugador.sumarDinero(jugador.getApuesta() * 2);
            refreshBets();
            reiniciarJuego();
        } else if (jugador.totalMano() == 21 && dealer.totalMano() == 21) {
            JOptionPane.showMessageDialog(this, "Empate por BlackJack");
        }
    }

    private void gameOver() {
        setVisible(false);
        JOptionPane.showMessageDialog(null, "Te quedaste sin plata. Perdiste.");
        System.exit(0);
    }

    private void refreshBets() {
        lblMoneyJugador.setText(" Dinero del Jugador: " + (jugador.getDinero()));
        lblMoneyDealer.setText(" Dinero del Dealer: " + (dealer.getDinero()));
    }

    private void mostrarCartas() {
        panelCartas.removeAll(); // Limpia el panel antes de agregar nuevas cartas
        for (Carta carta : jugador.getMano()) {
            JLabel cartaLabel = crearEtiquetaCarta(carta);
            panelCartas.add(cartaLabel);
        }
        panelCartas.revalidate(); // Vuelve a validar el panel
        panelCartas.repaint(); // Redibuja el panel
        lblPuntosJugador.setText(" Puntos Jugador: " + jugador.totalMano()); // Actualizar puntos
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
        lblPuntosDealer.setText(" Puntos Dealer: " + (mostrarTodasCartasDealer ? dealer.totalMano() : "??")); // Actualizar puntos
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
        if (dealer.totalMano() == 21) {
            JOptionPane.showMessageDialog(this, "El dealer gana por blackjack.");
            reiniciarJuego();
        }
        habilitarSeguro = false;
        btnSeguro.setEnabled(false);
        if (mazo.hayCartas()) {
            jugador.agregarCarta(mazo.sacarCarta());
            mostrarCartas();
            if (jugador.totalMano() > 21) {
                determinarGanador();
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
        if (dealer.totalMano() == 21) {
            JOptionPane.showMessageDialog(this, "El dealer gana por blackjack.");
            reiniciarJuego();
        }
        habilitarSeguro = false;
        btnSeguro.setEnabled(false);
        JOptionPane.showMessageDialog(this, jugador.getNombre() + " se ha plantado con un total de: " + jugador.totalMano());
        jugarDealer(); // Llama al dealer para que tome su turno
        mostrarTodasCartasDealer = true; // Revelar todas las cartas del dealer
        mostrarCartasDealer(); // Mostrar todas las cartas del dealer
        btnPedirCarta.setEnabled(false);
        btnPlantarse.setEnabled(false);
        btnVolverAJugar.setEnabled(true);
        detenerMusica();
    }

    private void seguro() {
        Carta carta = dealer.getMano().getLast();
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
        if (Objects.equals(valor, "ace")) {
            boolean success = false;
            while (!success) {
                try {
                    int apuesta = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el valor de apuesta: "));
                    if (apuesta != 0 && apuesta <= jugador.getDinero()) {
                        success = true;
                        if (dealer.totalMano() == 21 && habilitarSeguro) {
                            JOptionPane.showMessageDialog(this, "Ganaste la apuesta, cobraste " + apuesta * 2);
                            jugador.sumarDinero(apuesta * 2);
                            JOptionPane.showMessageDialog(this, "El dealer gana por blackjack");
                            refreshBets();
                        } else {
                            JOptionPane.showMessageDialog(this, "El dealer no tiene blackjack, perdiste " + apuesta);
                            jugador.restarDinero(apuesta);
                            habilitarSeguro = false;
                            btnSeguro.setEnabled(false);
                            refreshBets();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Error. Apuesta inválida.");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error, debes ingresar un numero. A menos de que quieras apostar tu casa...");
                }
            }
        } else{
            JOptionPane.showMessageDialog(this, "No puedes usar el seguro si el dealer no tiene un ace visible");
        }
    }

    private void jugarDealer() {
        // Inicia el turno del dealer y actualiza el estado de la apuesta
        while (true) {
            int dealerMoney = dealer.getDinero();
            int puntosDealer = dealer.totalMano();
            int cartasRestantes = mazo.cartasRestantes();
            double probabilidadDePasarse = calcularProbabilidadDePasarse(puntosDealer, cartasRestantes);

            // Condición para plantarse si el dealer tiene 20 o 21
            if (puntosDealer == 21 || puntosDealer == 20) {
                JOptionPane.showMessageDialog(this, "Dealer se planta con " + puntosDealer + " puntos.");
                break;
            }
            // Condición para plantarse si la probabilidad de pasarse es alta y tiene más de 17 puntos
            else if (puntosDealer >= 17 && probabilidadDePasarse >= 0.48) {
                JOptionPane.showMessageDialog(this, "Dealer se planta con " + puntosDealer + " puntos.");
                break;
            }
            // Si el dealer tiene menos de 17 puntos y la probabilidad de pasarse es baja, pide una carta
            else if (puntosDealer < 17 && probabilidadDePasarse < 0.54) {
                dealer.agregarCarta(mazo.sacarCarta());
                mostrarCartasDealer(); // Redibujar cada que el dealer juegue
                JOptionPane.showMessageDialog(this, "Dealer pide una carta. Ahora tiene: " + dealer.totalMano() + " puntos.");
                lblPuntosDealer.setText(" Puntos Dealer: " + dealer.totalMano()); // Actualizar puntos

                // Si el dealer se pasa de 21, finaliza el turno
                if (dealer.totalMano() > 21) {
                    JOptionPane.showMessageDialog(this, "Dealer se ha pasado con " + dealer.totalMano() + " puntos.");
                    break;
                }
            } else {
                dealer.agregarCarta(mazo.sacarCarta());
                mostrarCartasDealer(); // Redibujar cada que el dealer juegue
                JOptionPane.showMessageDialog(this, "Dealer pide una carta. Ahora tiene: " + dealer.totalMano() + " puntos.");
                lblPuntosDealer.setText(" Puntos Dealer: " + dealer.totalMano()); // Actualizar puntos

                // Si el dealer se pasa de 21, finaliza el turno
                if (dealer.totalMano() > 21) {
                    JOptionPane.showMessageDialog(this, "Dealer se ha pasado con " + dealer.totalMano() + " puntos.");
                    break;
                }
            }
        }

        // Determina el ganador y realiza la gestión de las apuestas
        determinarGanador();
    }


    private void determinarGanador() {
        int puntosJugador = jugador.totalMano();
        int puntosDealer = dealer.totalMano();
        int apuesta = jugador.getApuesta();
        int apuestaDealer = dealer.getApuesta();
        if (puntosDealer > 21 && puntosJugador <= 21) {
            // Gana el jugador
            if (puntosJugador == 21 && jugador.getMano().size() == 2) {
                // Gana con Blackjack (paga 3:2)
                int ganancia = (int) (apuesta * 1.5); // Pago 3:2
                dealer.restarDinero(apuestaDealer);
                jugador.sumarDinero(ganancia + apuesta);
                JOptionPane.showMessageDialog(this, "¡Felicidades! Ganaste con Blackjack y ganas " + ganancia);
            } else {
                // Gana de forma normal
                dealer.restarDinero(apuestaDealer);
                jugador.sumarDinero(apuesta * 2); // Recupera su apuesta + ganancia igual a la apuesta
                JOptionPane.showMessageDialog(this, "¡Felicidades! Ganaste con " + puntosJugador + " puntos y ganas " + apuesta);
            }
        } else if (puntosJugador <= 21 && puntosJugador > puntosDealer) {
            // Gana el jugador de forma normal
            jugador.sumarDinero(apuesta * 2); // Recupera su apuesta + ganancia igual a la apuesta
            dealer.restarDinero(apuestaDealer);
            JOptionPane.showMessageDialog(this, "¡Felicidades! Ganaste con " + puntosJugador + " puntos y ganas " + apuesta);
        } else if (puntosJugador == puntosDealer) {
            // Empate, no se modifica el dinero
            JOptionPane.showMessageDialog(this, "Es un empate. Recuperas tu apuesta.");
        } else {
            // Gana el dealer o se pasa el jugador
            JOptionPane.showMessageDialog(this, "El dealer gana con " + puntosDealer + " puntos.");
            dealer.sumarDinero(apuestaDealer * 2);
        }

        mostrarTodasCartasDealer = true;
        mostrarCartasDealer();
        refreshBets();

    }

    private double calcularProbabilidadDePasarse(int puntosDealer, int cartasRestantes) {
        // Verificar condiciones especiales
        if (puntosDealer >= 21) return 1.0; // El dealer ya está en o por encima de 21, se va a pasar.
        if (cartasRestantes == 0) return 0.0; // Evita división por cero si no hay cartas restantes.

        // Cartas que podrían hacer que el dealer se pase
        int cartasQueHacenPasarse = 0;

        // Optimizar obteniendo las cartas restantes solo una vez
        ArrayList<Carta> cartasRestantesMazo = mazo.getCartasRestantes();

        // Contamos las cartas que harán que el dealer se pase
        for (Carta carta : cartasRestantesMazo) {
            int valorCarta = carta.valorEnPuntos();

            // Ajustar para As, si es necesario
            if (carta.getValor().equals("A") && puntosDealer + 11 > 21) {
                valorCarta = 1; // Si agregar 11 pasa al dealer, el As cuenta como 1.
            }

            if (puntosDealer + valorCarta > 21) {
                cartasQueHacenPasarse++;
            }
        }
        double probablidad = (double) cartasQueHacenPasarse / cartasRestantesMazo.size();
        return probablidad; // Retorna la probabilidad
    }


    private void reiniciarJuego() {
        String[] options = new String[]{"Si", "No", "Salir"};
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
        lblPuntosJugador.setText(" Puntos Jugador: 0");
        lblPuntosDealer.setText(" Puntos Dealer: 0");
        btnPedirCarta.setEnabled(true); // Habilitar botón "Pedir Carta"
        btnPlantarse.setEnabled(true); // Habilitar botón "Plantarse"
        btnSeguro.setEnabled(true); // Habilitar boton "Seguro"
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

    private void fold() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Juego juego = new Juego();
            juego.setVisible(true);
        });
    }
}
