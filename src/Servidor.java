import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor extends Thread {
    private ArrayList<ClientInfo> jugadores;
    private ServerSocket serverSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream writer;
    private Protocol protocol;
    private Mazo mazo;
    public Servidor() {
        jugadores = new ArrayList<>();
        protocol = new Protocol();
        mazo = new Mazo();
        try {
            serverSocket=new ServerSocket(Integer.parseInt(JOptionPane.showInputDialog("Ingrese el puerto que desee usar para el servidor")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        Socket clientSocket = null;
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String sentence = "";
            try {
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                sentence = dataInputStream.readUTF();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (sentence.startsWith("CONNECT")) {
                String pname = sentence.split(";")[1];
                try {
                    writer = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                sendToClient(protocol.IDPacket(jugadores.size() + 1));
                try {
                    BroadCastMessage(protocol.NewPlayerPacket(jugadores.size() + 1, pname));
                    sendAllClients(writer, pname);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                jugadores.add(new ClientInfo(writer, pname));
            }
        }
    }

    public void BroadCastMessage(String mess) throws IOException
    {
        for(int i=0; i < jugadores.size();i++)
        {
            if(jugadores.get(i)!=null)
                jugadores.get(i).getWriterStream().writeUTF(mess);
        }
    }
    public void sendToClient(String message)
    {
        if(message.equals("exit"))
            System.exit(0);
        else
        {
            try {
                writer.writeUTF(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendAllClients(DataOutputStream writer, String pname)
    {
        for(int i=0;i<jugadores.size();i++)
        {
            if(jugadores.get(i)!=null) {
                try {
                    writer.writeUTF(protocol.NewPlayerPacket(i + 1, pname));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public static class ClientInfo
    {
        private String nombre;
        private int dinero; // Dinero del jugador
        DataOutputStream dataOutputStream;
        ArrayList<Carta> mano;

        public ClientInfo(DataOutputStream dataOutputStream, String pname)
        {
            this.dataOutputStream = dataOutputStream;
            this.nombre  = pname;
        }
        public DataOutputStream getWriterStream()
        {
            return dataOutputStream;
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
}
