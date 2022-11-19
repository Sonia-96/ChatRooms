package com.example.androidchatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatroomActivity extends AppCompatActivity {

    private String userName;
    protected static ArrayList<String> messages = new ArrayList<>();
    protected static ListView messageListView;
    protected static ArrayAdapter<String> messageAdapter;
    protected static ArrayList<String> clients = new ArrayList<>();
    protected static ListView userListView;
    protected static ArrayAdapter<String> userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //UI thread
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        Intent intent = getIntent();
        userName = intent.getStringExtra("user");
        String roomName = intent.getStringExtra("room");

        TextView roomNameTV = findViewById(R.id.roomName);
        roomNameTV.setText(roomName);

        TextView sendMessageView = findViewById(R.id.messageText);
        sendMessageView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    handleSend(view);
                    return true;
                }
                return false;
            }
        });

        // set message listView
        messageListView = findViewById(R.id.messagesListView);
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        messageListView.setAdapter(messageAdapter);

        // set user listView
        userListView = findViewById(R.id.userListView);
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, clients);
        userListView.setAdapter(userAdapter);
    }


    public void handleLogout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void handleSend(View view) {
        TextView messageView = findViewById(R.id.messageText);
        String message = messageView.getText().toString();
        LoginActivity.ws_.sendText(userName + " " + message);
        messageView.setText("");
    }
}
