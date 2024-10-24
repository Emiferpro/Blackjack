import java.util.ArrayList;

public class Jugador {
    private String nombre;
    private ArrayList<Carta> mano;
    private int dinero; // Dinero del jugador
    private int apuesta;
    public Jugador(String nombre) {
        this.nombre = nombre;
        mano = new ArrayList<>();
        dinero = 100; // Dinero inicial
        apuesta = 0;
    }

    public int getApuesta() {
        return apuesta;
    }
    public void setApuesta(int ap) {
        this.apuesta = ap;
        this.dinero -= ap;
    }

    public void agregarCarta(Carta carta) {
        mano.add(carta);
    }

    public int totalMano() {
        int total = 0;
        int ases = 0;

        for (Carta carta : mano) {
            total += carta.valorEnPuntos();
            if (carta.getValor().equals("A")) {
                ases++;
            }
        }

        // Ajustar el valor del As si es necesario
        while (total > 21 && ases > 0) {
            total -= 10; // Cambia el valor del As de 11 a 1
            ases--;
        }

        return total;
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<Carta> getMano() {
        return mano;
    }

    public int getDinero() {
        return dinero;
    }

    public void sumarDinero(int cantidad) {
        dinero += cantidad;
    }

    public void restarDinero(int cantidad) {
        dinero -= cantidad;
    }

    @Override
    public String toString() {
        return nombre + " tiene " + mano.toString() + " y $" + dinero;
    }
}
