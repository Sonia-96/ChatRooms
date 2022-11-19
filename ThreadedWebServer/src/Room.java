import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class implements a Web Chat Room.A room can add/remove clients, store all messages, and send messages to all clients.
 */

public class Room {

    private final static Map<String, Room> rooms_ = new HashMap<>();
    private final String name_;
    private final ArrayList<Client> clients_; // clients in this room
    private final PrintWriter messageWriter;


    public Room(String name) throws IOException {
        name_ = name;
        clients_ = new ArrayList<>();
        File messageFile = new File("data/" + name + ".txt");
//        if (!messageFile.exists()) {
//            messageFile.createNewFile();
//        }
        messageWriter = new PrintWriter(messageFile);
    }

    public synchronized void addClient(Client client) {
        if (!clients_.contains(client)) {
            clients_.add(client);
        }
        if (!Client.clients_.contains(client)) {
            Client.clients_.add(client);
        }
        client.addRoom(name_);
    }

    public synchronized void removeClient(Client client) {
        clients_.remove(client);
    }

    /**
     * When a new client enter the web chat room, send the clients and messages of each room to the new client.
     * The format of the message:
     * {
     *     rooms: {
     *         room1: {
     *             clients: { ..., ..., ...};
     *             messages: { ..., ..., ...}'
     *         },
     *         room2: ... ,
     *     },
     *     type : roominfo;
     * }
     * @param out
     */
    public synchronized void sendRoomsInfo(OutputStream out) throws IOException, ParseException {
        JSONObject roomsJson = new JSONObject();
        roomsJson.put("type", "roomInfo");
        for (String key : rooms_.keySet()) {
            Room room = rooms_.get(key);
            JSONObject roomJson = new JSONObject();
            roomJson.put("name", room.getName());
            // send client list
            JSONArray clientsJson = new JSONArray();
            for (Client client: room.getClients()) {
                clientsJson.add(client.getName());
            }
            // send all messages (including message, join, & leave)
            JSONArray messagesJson = new JSONArray();
            File file = new File("data/" + key + ".txt");
            Scanner reader = new Scanner(file);
            JSONParser parser = new JSONParser();
            while (reader.hasNextLine()) {
                String msg = reader.nextLine();
                JSONObject msgJson = (JSONObject) parser.parse(msg);
                messagesJson.add(msgJson);
            }
            roomJson.put("clients", clientsJson);
            roomJson.put("messages", messagesJson);
            roomsJson.put(room.getName(), roomJson);
        }
        MessageTools.sendMessage(roomsJson.toString(), out);
    }

    public synchronized void sendMessageToAllClients(String message) throws IOException {
        for (Client client : clients_) {
            MessageTools.sendMessage(message, client.getOutputStream());
        }
    }

    public synchronized void addMessage(String message) {
        messageWriter.println(message);
        messageWriter.flush();
    }

    public ArrayList<Client> getClients() { // should it be syncrhonized?
        return clients_;
    }

    /**
     * If the room name already exists, return the room. Otherwise, create and return a new room.
     * @param name - room name
     * @return
     */
    public synchronized static Room getRoom(String name) throws IOException {
        if (rooms_.containsKey(name)) {
            return rooms_.get(name);
        }
        Room room = new Room(name);
        rooms_.put(name, room);
        return  room;
    }

    public String getName() {
        return name_;
    }
}
