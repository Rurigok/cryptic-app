package net.cryptic.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import me.himanshusoni.chatmessageview.ChatMessageView;

public class ChatActivity extends AppCompatActivity {

    private ChatMessageView mChatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String contact = intent.getStringExtra("CONTACT_NAME");
        Log.i("OUTPUT", "Contact Name: " + contact);
        getSupportActionBar().setTitle(contact);
    }
}