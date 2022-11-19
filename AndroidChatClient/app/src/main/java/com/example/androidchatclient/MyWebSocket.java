package com.example.androidchatclient;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MyWebSocket extends WebSocketAdapter {

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        Log.d("CC:MyWebSocket", "Web Socket Connected!");
    }

    @Override
    public void onConnectError(WebSocket webSocket, WebSocketException exception) {
        Log.d("CC:MyWebSocket", "Socket Connect Failed!");
    }

    @Override
    public void onError(WebSocket webSocket, WebSocketException cause) {
        Log.d("CC:MyWebSocket", "An error occurred");
    }

    @Override
    public void onTextMessage(WebSocket webSocket, String message) throws JSONException {
        Log.d("CC:MyWebSocket", message);
        JSONObject jsonObject = new JSONObject(message);
        String type = jsonObject.getString("type");
        String username = jsonObject.getString("user");
        String roomname = jsonObject.getString("room");
        if (type.equals("join")) {
            ChatroomActivity.messages.add(username + " joins " + roomname);
            ChatroomActivity.clients.add(username);
        } else if (type.equals("message")) {
            ChatroomActivity.messages.add(username + ": " + jsonObject.getString("message"));
        } else if (type.equals("leave")) {
            ChatroomActivity.messages.add(username + " leaves " + roomname);
            ChatroomActivity.clients.remove(username);
        }

        ChatroomActivity.messageListView.post(new Runnable() {
            @Override
            public void run() {
                ChatroomActivity.messageAdapter.notifyDataSetChanged();
                ChatroomActivity.messageListView.smoothScrollToPosition(ChatroomActivity.messageAdapter.getCount());
            }
        });

        ChatroomActivity.userListView.post(new Runnable() {
            @Override
            public void run() {
                ChatroomActivity.userAdapter.notifyDataSetChanged();
                ChatroomActivity.userListView.smoothScrollToPosition(ChatroomActivity.userAdapter.getCount());
            }
        });
    }
}
