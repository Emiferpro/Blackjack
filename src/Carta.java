public class Carta {
    private final String palo; // Corazones, Diamantes, Tréboles, Picas
    private final String valor; // 2-10, J, Q, K, A

    public Carta(String palo, String valor) {
        this.palo = palo;
        this.valor = valor;
    }

    public static Carta getEmpty() {
        return new Carta("null", "null");
    }

    public String getPalo() {
        return palo;
    }

    public String getValor() {
        return valor;
    }

    // Método para obtener el valor en puntos de la carta
    public int valorEnPuntos() {
        switch (valor) {
            case "J", "Q", "K" -> { return 10; }

            case "A" -> {
                return 11; // Ace puede ser 1 o 11, se manejará en la clase Jugador
            }
            default -> {
                return Integer.parseInt(valor);
            }
        }
    }

    @Override
    public String toString() {
        return valor + " de " + palo;
    }
}
