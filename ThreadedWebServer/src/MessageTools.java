import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;

/**
 *  MessagesTools class provides helper functions for Room class and HTTPResponse class to create and send message.
 */

public class MessageTools {
    private static JSONObject createMessage(String type, String username, String roomname, String timestamp) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("user", username);
        jsonObject.put("room", roomname);
        jsonObject.put("time", timestamp);
        return jsonObject;
    }

    public static String createLeaveMessage(String username, String roomname, String timestamp) {
        JSONObject jsonObject = createMessage("leave", username, roomname, timestamp);
        return jsonObject.toString();
    }

    public static String createJoinMessage(String username, String roomname, String timestamp) {
        JSONObject jsonObject = createMessage("join", username, roomname, timestamp);
        return jsonObject.toString();
    }

    public static String createChatMessage(String username, String roomname, String message, String timestamp) {
        JSONObject jsonObject = createMessage("message", username, roomname, timestamp);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }

    /**
     * Send message under Web Socket Protocol.
     * @param message - the String of message to be sent
     * @param client - the output stream of the client
     * @throws IOException
     */
    public static void sendMessage(String message, OutputStream client) throws IOException {
        DataOutputStream out = new DataOutputStream(client);
        int payloadLen = message.length();
        out.writeByte(0x81);
        if (payloadLen < 126) {
            out.write(message.length());
        } else if (payloadLen < Math.pow(2, 16)) {
            out.write(126);
            out.writeShort(payloadLen);
        } else {
            out.write(127);
            out.writeLong(payloadLen);
        }
        out.writeBytes(message);
        client.flush();
    }
}
