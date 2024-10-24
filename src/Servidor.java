import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Servidor extends Thread {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Servidor() {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(JOptionPane.showInputDialog("Ingresa un puerto para el servidor:")));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Oops. Ocurrió un error.");
        }
    }
    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();
                System.out.println(clientSocket.getInetAddress().getHostAddress() + " Connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Oops. Ocurrió un error.");
        }
    }
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientUserName;


        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.clientUserName = bufferedReader.readLine();
                clientHandlers.add(this);
                broadCastMessage("SERVER: " + clientUserName + " has joined the game!");
            } catch (IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
            }
        }

        @Override
        public void run() {
            String messageFromClient;
            while (socket.isConnected()) {
                try {
                    messageFromClient = bufferedReader.readLine();
                    broadCastMessage(messageFromClient);
                } catch (IOException e) {
                    closeAll(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }


        // Send a message to everyone except me.
        public void broadCastMessage(String messageToSend) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.clientUserName.equals(this.clientUserName)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeAll(socket, bufferedReader, bufferedWriter);
                }
            }
        }

        // Client left the chat.
        public void removeClientHandler() {
            clientHandlers.remove(this);
            broadCastMessage("SERVER: " + clientUserName + " has left the game!");
        }

        public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
            removeClientHandler();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

