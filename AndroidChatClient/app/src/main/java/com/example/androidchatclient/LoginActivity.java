package com.example.androidchatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    static WebSocket ws_;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // UI thread
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            ws_ = new WebSocketFactory().createSocket("ws://10.0.2.2:8080/", 1000 );
        }
        catch( IOException e ) {
            Log.e( "Dd:","WS error" );
        }
        ws_.addListener( new MyWebSocket() );
        ws_.connectAsynchronously();
    }

    public void handleLogin(View view) {
        TextView roomNameTV = findViewById(R.id.roomNameText);
        String room = roomNameTV.getText().toString();
        TextView userNameTV = findViewById(R.id.userNameText);
        String user = userNameTV.getText().toString();
        ws_.sendText("join " + user + " " + room);
        Intent intent = new Intent(LoginActivity.this, ChatroomActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}