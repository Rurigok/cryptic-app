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
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static String storedOrReceived;
    public static int deletionTimer;
    public static String flags;
    public static String nonceString;
    private List<JSONObject> messages;
    private FileOutputStream outputStream;
    private ListView mListView;
    private AutoCompleteTextView mMessageView;
    List<String> stringList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.listview, stringList);
        //ArrayAdapter<String> msgAdapter = new ArrayAdapter<String>(this, R.layout.conversation_list, R.id.message, msgList);

        Intent intent = getIntent();
        String contact = intent.getStringExtra("CONTACT_NAME");
        Log.i("OUTPUT", "Contact Name: " + contact);
        getSupportActionBar().setTitle(contact);


        FileInputStream inputStream;

        // Try to open file
        try {
            inputStream = openFileInput(contact + ".txt");
            //inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + contact + ".txt");
            this.outputStream = openFileOutput(contact + ".txt", Context.MODE_PRIVATE);
            StringBuilder fileContent = new StringBuilder();
            Log.i("FILE READ", "FILE DOES EXIST");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inputStream.read(buffer)) != -1)
            {
                Log.i("FILE READ", "JUST READ: " + buffer.toString());
                fileContent.append(new String(buffer, 0, n));
            }
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
        }


        /*
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

        */

        //msgList = new ListView(this);

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

        Log.i("Output", "SENDING MESSAGE:" + message);
        if (mMessageView.length() > 0) {
            mMessageView.getText().clear();
        }
        String str = message.toString();
        try {
            outputStream.write(str.getBytes());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}