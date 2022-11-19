import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class implements an HTTP response. The class takes in an HTTP request.
 *
 * <p>- If it's a normal HTTP request, the requested file will be sent back.
 *
 * <p>- If it's a web socket request, a web socket connection will be built.
 * Then the server will keep handling client's messages and send messages back.
 *
 * <p>This class uses Room class to store and send messages to clients, and
 * use MessageTools to create a message. </p>
 */

public class HTTPResponse {
    private final HTTPRequest request_;
    private final PrintWriter printWriter;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private Client client_;
    private boolean firstJoin_;

    HTTPResponse(HTTPRequest req, OutputStream out, InputStream in) {
        request_ = req;
        outputStream = out;
        inputStream = in;
        printWriter = new PrintWriter(outputStream);
        firstJoin_ = true;
    }

    /**
     * Send response to a request.
     * <p>If the request is a web socket request, first start handshake with the client, then keep handling client's message until they leave the room</p>
     * <p>If the request is a normal HTTP request, send a normal HTTP response back.</p>
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    protected void sendResponse() throws IOException, NoSuchAlgorithmException, ParseException {
        if (request_.isWebSocketRequest()) {
            System.out.println("this is a web socket request! " + Thread.currentThread().getName());
            handShake();
            boolean stop = false;
            while (!stop) {
                byte[] msg = decodeMessage(inputStream);
                System.out.println(new String(msg));
                stop = handleMessage(msg);
//                if (opcode_ == 0x8) { // the client leaves the room
//                    if (room_ != null) {
//                        room_.removeClient(client_);
//                        room_.sendMessageToAllClients(MessageTools.createLeaveMessage(username_, room_.getName(), "nihao"));
//                    }
//                    break;
//                } else {
//                    handleMessage(msg);
//                }
            }
        } else {
            sendNormalHTTPResponse();
        }
    }

    /**
     * Send the server's handshake response
     * @throws NoSuchAlgorithmException
     */
    private void handShake() throws NoSuchAlgorithmException {
        printWriter.println("HTTP/1.1 101 Switching Protocols");
        printWriter.println("Upgrade: websocket");
        printWriter.println("Connection: Upgrade");
        printWriter.println("Sec-WebSocket-Accept: " + generateAcceptString(request_.getHeaders().get("Sec-WebSocket-Key")));
        printWriter.println();
        printWriter.flush();
    }

    /**
     * Decode a web socket request and get the decoded payload.
     * @param in - the client's input stream
     * @return decoded payload
     * @throws IOException
     */
    public static byte[] decodeMessage(InputStream in) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);
        byte b0 = dataInputStream.readByte();
        boolean fin = (b0 & 0x80) != 0; // TODO handle fin is false
        byte opcode_ = (byte) (b0 & 0x0F);
        byte b1 = dataInputStream.readByte();
        boolean masked = (b1 & 0x80) != 0;
        int len = b1 & 0x7F;
        if (len == 126) {
            len = dataInputStream.readShort(); // 2 bytes
        } else if (len == 127) {
            len =  (int) dataInputStream.readLong(); // 8 bytes
        }
        // unmask data
        byte[] decoded;
        if (masked) {
            byte[] mask = dataInputStream.readNBytes(4);
            byte[] encoded = dataInputStream.readNBytes(len);
            decoded = new byte[encoded.length];
            for (int i = 0; i < encoded.length; i++) {
                decoded[i] = (byte) (encoded[i] ^ mask[i % 4]);
            }
        } else {
            decoded = dataInputStream.readNBytes(len);
        }
        return decoded;
    }

    /**
     * Handle the messaged received from the client.
     * <p>There are three types of message with specific formats:</p>
     * <p>- join username roomname</p>
     * <p>- message username message</p>
     * <p>- leave username roomname</p>
     * This method only handles "join" and "message" message.
     * @param msg - the message bytes received from the client.
     * @throws IOException
     */
    private boolean handleMessage(byte[] msg) throws IOException, ParseException {
        String string = new String(msg, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(string);

        String type = (String) jsonObject.get("type");
        String roomName = (String) jsonObject.get("room");
        Room room = Room.getRoom(roomName);
        String username = (String) jsonObject.get("user");
        String timestamp = (String) jsonObject.get("time");
        String message = null;
        boolean isLeaveMessage = false;
        if (type.equals("join")) {
            if (firstJoin_) {
                client_ = new Client(username, outputStream);
                room.sendRoomsInfo(outputStream);
                firstJoin_ = false;
            }
            room.addClient(client_);
            message = MessageTools.createJoinMessage(username, room.getName(), timestamp);
            Client.sendMessageToAllClients(message);
        } else if (type.equals("message")) {
            message = MessageTools.createChatMessage(username, room.getName(), (String) jsonObject.get("message"), timestamp);
            Client.sendMessageToAllClients(message);
        } else if (type.equals("leave")) {
            // send leave message to all rooms that clients are in
            for (String name : client_.getRooms()) {
                Room.getRoom(name).removeClient(client_);
                message = MessageTools.createLeaveMessage(username, name, timestamp);
                Client.sendMessageToAllClients(message);
            }
            isLeaveMessage = true;
        }
        room.addMessage(message);
        return isLeaveMessage;
    }

    /**
     * Generate a Sec-WebSocket-Accept String according to a specific Sec-WebSocket-Key.
     * @param key - the value of Sec-WebSocket-Key in the request headers
     * @return corresponding Sec-WebSocket-Accept String
     * @throws NoSuchAlgorithmException
     */
    public static String generateAcceptString(String key) throws NoSuchAlgorithmException {
        String key2 = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(key2.getBytes()); // SHA-1 hashing
        return Base64.getEncoder().encodeToString(md.digest()); // Base64 encoding
    }

    /**
     * Handle normal HTTP requests. Send the requested file to the client.
     * @throws IOException
     */
    private void sendNormalHTTPResponse() throws IOException {
        File file = request_.getFile();
        String filename = file.getName();
        boolean fileExists = !filename.equals("404.html");
        printWriter.println("HTTP/1.1 " + (fileExists ? "200 OK" : "404 Not Found"));
        // get extension
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (extension.equals("jpeg")) {
            printWriter.println("Content-Type: image/" + extension);
        } else {
            printWriter.println("Content-Type: text/" + extension);
        }
        printWriter.println("Content-Length: " + file.length());
        printWriter.print("\n");
        printWriter.flush();
        // flush the file
        FileInputStream fStream = new FileInputStream(file);
        for (int i = 0; i < file.length(); i++) {
            outputStream.write( fStream.read() );
            outputStream.flush();
//                Thread.sleep(10);
        }
        printWriter.flush();
        printWriter.close();
    }
}
