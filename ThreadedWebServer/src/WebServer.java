import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class implements a multi-thread web server. The server waits for clients to come
 * then create a thread to handle the connection. The actual work is performed
 * by an instance of ConnectionHandler class.
 *
 * <p> This web server supports the operation of a *eb Chat Room. The implementations of the
 * web chat are stored in the folder "resources".
 */

public class WebServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        ServerSocket server;
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Server failed to start...");
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                // connect to the client
                Socket clientSocket = server.accept();
                Thread thread = new Thread(new ConnectionHandler(clientSocket));
                thread.start();
            } catch (IOException e) {
                System.out.println( "An error occurred while talking to client, closing connection to client.");
            }
        }
    }
}