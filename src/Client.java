import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private String nickname;

    public Client(Socket socket, String nickname) {
        try {
            this.socket = socket;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter((new OutputStreamWriter(socket.getOutputStream())));
            this.nickname = nickname;
        } catch (IOException e) {
            closeEverything(socket, input, output);
        }
    }

    public void sendMessage() {
        try {
            output.write(nickname);
            output.newLine();
            output.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                output.write(nickname + ": " + message);
                output.newLine();
                output.flush();
            }
            scanner.close();
        } catch( IOException e) {
            closeEverything(socket, input, output);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String messageFromChat;

                while (socket.isConnected()) {
                    try {
                        messageFromChat = input.readLine();
                        if (messageFromChat == null) break;
                        System.out.println(messageFromChat);
                    } catch (IOException e) {
                        closeEverything(socket, input, output);
                    }

                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader input, BufferedWriter output) {
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your nickname: ");
        String nickname = scanner.nextLine();

        Socket socket = new Socket("localhost", 8080);
        Client client = new Client(socket, nickname);
        client.listenForMessage();
        client.sendMessage();
        scanner.close();
    }

}
