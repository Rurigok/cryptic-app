package net.cryptic.app;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.himanshusoni.chatmessageview.ChatMessageView;

public class ChatActivity extends AppCompatActivity {

    private List<JSONObject> messages;
    private ListView msgList;
    private AutoCompleteTextView mMessageView;
    List<String> stringList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.listview, stringList);

        Intent intent = getIntent();
        String contact = intent.getStringExtra("CONTACT_NAME");
        Log.i("OUTPUT", "Contact Name: " + contact);
        getSupportActionBar().setTitle(contact);

        try {
            FileInputStream in = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + contact + ".txt");
            StringBuilder fileContent = new StringBuilder();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }
            in.close();
            Log.i("Inform me", fileContent.toString());
        } catch(FileNotFoundException e){
            //System.exit(-1);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        msgList = (ListView) findViewById(R.id.chatView);
        msgList.setAdapter(adapter);


        mMessageView = (AutoCompleteTextView) findViewById(R.id.messageToSend);
        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == R.id.sendMessage || id == EditorInfo.IME_NULL) {
                    sendMessage();
                    return true;
                }
                return false;
            }


        });
    }

    private void sendMessage() {

        Editable message = mMessageView.getText();

        Log.i("Output", "SENDING MESSAGE: " + message);
        mMessageView.setText("");
    }
}