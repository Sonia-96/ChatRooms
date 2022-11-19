import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * This class implements a client, which has two fields: name_ and outputStream_.
 */
public class Client {
    private final String name_;
    private final OutputStream outputStream_;
    private final Set<String> rooms_;
    protected static final Set<Client> clients_ = new HashSet<>(); // clients in all rooms

    public Client(String n, OutputStream o) {
        name_ = n;
        outputStream_ = o;
        rooms_ = new HashSet<>();
    }

    public static void sendMessageToAllClients(String message) throws IOException {
        for (Client client : clients_) {
            System.out.println("sent to " + client.name_ + " " + message);
            MessageTools.sendMessage(message, client.outputStream_);
        }
    }

    public OutputStream getOutputStream() {
        return outputStream_;
    }

    public String getName() {
        return name_;
    }

    public void addRoom(String room) {
        rooms_.add(room);
    }

    public Set<String> getRooms() {
        return rooms_;
    }
}
