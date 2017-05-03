package net.cryptic.app;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static String flags;
    public static JsonUtil jsonUtil = new JsonUtil();
    private FileOutputStream outputStream;
    private ListView mListView;
    private Button composeMessage;
    List<StoredMessage> messages = new ArrayList<>();
    List<JSONObject> jsons = new ArrayList<>();
    List<String> stringList = new ArrayList<>();
    private ChatAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        adapter = new ChatAdapter(this, new ArrayList<StoredMessage>());

        Intent intent = getIntent();
        String contact = intent.getStringExtra("CONTACT_NAME");
        Log.i("OUTPUT", "Contact Name: " + contact);
        getSupportActionBar().setTitle(contact);
        StringBuilder fileContent = new StringBuilder();

        FileInputStream inputStream;
        JSONObject json = null;
        // Try to open file
        try {
            inputStream = openFileInput(contact + ".txt");
            Log.i("FILE READ", "FILE DOES EXIST");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inputStream.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }
            Log.i("FILE READ", "JUST READ: " + fileContent.toString());

            String[] temps = fileContent.toString().split("---separator---");
            for(String str : temps)
                jsons.add(new JSONObject((str)));

            inputStream.close();
        } catch (FileNotFoundException f) {
            Log.i("NOTIFY", "NO FILE FOUND: " + f.getMessage());
            //Try to create file
            try {
                this.outputStream = openFileOutput(contact + ".txt", Context.MODE_PRIVATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        //Create fake JSON message for now
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("contact", contact);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonObj.put("message", "THE FIRST MESSAGE TEST");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mListView = (ListView) findViewById(R.id.messageList);
        mListView.setAdapter(adapter);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);

        for(JSONObject j : jsons) {
            try {
                StoredMessage msg = new StoredMessage(j.getString("message"), j.getInt("deletionTimer"));
                msg.sentOrReceived = j.getString("sentOrReceived");
                adapter.add(msg);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        composeMessage = (Button) findViewById(R.id.composeMessage);
        composeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ComposeMessage.class);
                intent.putExtra("personal_key", getIntent().getStringExtra("personal_key"));
                intent.putExtra("CONTACT_NAME", getIntent().getStringExtra("CONTACT_NAME"));
                startActivity(intent);
            }
        });
    }

    /*private void sendMessage() {

        Editable message = mMessageView.getText();
        StoredMessage msg = new StoredMessage(message.toString());
        msg.sentOrReceived = "SENT";
        msg.type = "STORED";
        Log.i("Output", "SENDING MESSAGE:" + message);
        if (mMessageView.length() > 0) {
            mMessageView.getText().clear();
        }
        //String str = message.toString();
        try {
            outputStream.write(jsonUtil.toJSon(msg).getBytes());
            Log.i("STORED IN", getFilesDir().getAbsolutePath().toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }*/
}