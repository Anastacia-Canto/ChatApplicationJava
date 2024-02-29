import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String nickname;
    private BufferedReader input;
    private BufferedWriter output;


    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            nickname = input.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: " + nickname + " has entered the chat!");
            output.write("Hello " + nickname + ", welcome to chat!");
            output.newLine();
            output.flush();
        } catch (IOException e) {
            closeEverything(socket, input, output);
        }

    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected())  {
            try {
                messageFromClient = input.readLine();
                if (messageFromClient == null || messageFromClient.equalsIgnoreCase(nickname + ": QUIT")) {
                    closeEverything(socket, input, output);
                    break;
                }
                broadcastMessage(messageFromClient);
            } catch(IOException e) {
                closeEverything(socket, input, output);
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clientHandlers) {
            try {
                if (!client.nickname.equals(nickname)) {
                    client.output.write(message);
                    client.output.newLine();
                    client.output.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, input, output);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server: " + nickname + " has left the chat!");
        System.out.println("Connection lost with client " + nickname);
    }

    public void closeEverything(Socket socket, BufferedReader input, BufferedWriter output) {
        removeClientHandler();
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
