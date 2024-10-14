import java.util.ArrayList;
import java.util.Collections;

public class Mazo {
    private final ArrayList<Carta> cartas;

    public Mazo() {
        cartas = new ArrayList<>();
        String[] palos = {"Corazones", "Diamantes", "Tréboles", "Picas"};
        String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String palo : palos) {
            for (String valor : valores) {
                cartas.add(new Carta(palo, valor));
            }
        }

        Collections.shuffle(cartas); // Mezcla el mazo
    }

    public Carta sacarCarta() {
        return cartas.removeLast(); // Saca la carta de arriba del mazo
    }

    public boolean hayCartas() {
        return !cartas.isEmpty();
    }
    public int cartasRestantes() {
        return cartas.size(); // Retorna el número de cartas restantes en el mazo
    }

    public ArrayList<Carta> getCartasRestantes() {
        return cartas;
    }
}
