package net.cryptic.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScrollingActivity extends AppCompatActivity {

    List<Conversation> conversations = new ArrayList<>();
    ListView convos;
    List<String> stringList = new ArrayList<>();
    private String PREFS_NAME = "CRYPTIC_DATA";
    JsonUtil jsonUtil = new JsonUtil();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button signOut = (Button) findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("Debug", "SIGNED OUT! Technically...not really.");

                SharedPreferences settings = getApplication().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("cookie");
                editor.apply();
                Log.i("OUTPUT", "Cookie removed.");

                Intent intent = new Intent(ScrollingActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        Date date = new Date();
        Date date2 = new Date();
        Date date3 = new Date();
        try {
            date2 = format.parse("2017/04/26 12:24:34");
            date3 = format.parse("2016/04/19 12:24:34");
        } catch (ParseException e){
            e.printStackTrace();
        }

        IntentFilter statusIntentFilter = new IntentFilter("MESSAGE_RECEIVED");
        ConnectSender sender = new ConnectSender();
        LocalBroadcastManager.getInstance(this).registerReceiver(sender, statusIntentFilter);

        Conversation convo = new Conversation("Andrew", date);
        Conversation convo2 = new Conversation("Edward", date2);
        Conversation convo3 = new Conversation("Sean", date3);
        StoredMessage mes = new StoredMessage("MINE!", 10);
        StoredMessage mes2 = new StoredMessage("YOURS!", 20);
        StoredMessage mes3 = new StoredMessage("I Andrew like paragon", 30);

        StoredMessage met = new StoredMessage("Hey! They have funnel cakes!", 20);
        StoredMessage met2 = new StoredMessage("Hello", 40);
        StoredMessage met3 = new StoredMessage("Hi", 30);

        StoredMessage mez = new StoredMessage("Bananas are pretty cool.", 20);
        StoredMessage mez2 = new StoredMessage("Hello?", 40);
        StoredMessage mez3 = new StoredMessage("They're out of apples...", 30);

        mes.sentOrReceived = "SENT";
        mes2.sentOrReceived = "RECEIVED";
        mes3.sentOrReceived = "RECEIVED";

        met.sentOrReceived = "SENT";
        met2.sentOrReceived = "RECEIVED";
        met3.sentOrReceived = "SENT";

        mez.sentOrReceived = "SENT";
        mez2.sentOrReceived = "SENT";
        mez3.sentOrReceived = "SENT";

        convo.getMessages().add(mes3);
        convo.getMessages().add(mes);
        convo.getMessages().add(mes2);

        convo2.getMessages().add(met3);
        convo2.getMessages().add(met2);
        convo2.getMessages().add(met);

        convo3.getMessages().add(mez);
        convo3.getMessages().add(mez2);
        convo3.getMessages().add(mez3);

        conversations.add(convo);
        conversations.add(convo2);
        conversations.add(convo3);

        FileOutputStream out;
        for(Conversation c : conversations)
        {
            stringList.add(c.getDisplay());
            Log.i("Inform me", "NOW!");
            try {
                if(!fileExists(c.getFromUser() + ".txt")) {
                    out = openFileOutput(c.getFromUser() + ".txt", Context.MODE_PRIVATE);
                    for (StoredMessage sm : c.getMessages()) {
                        out.write(jsonUtil.toJSon(sm).getBytes());
                        out.write("---separator---".getBytes());
                    }
                    out.flush();
                    out.close();
                }

                Log.i("Inform me", "YO!");
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(JSONException e){
                e.printStackTrace();
            }
            Log.d("OUTPUT", c.getFromUser());
            Log.d("OUTPUT", c.getDate().toString());
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.listview, stringList);

        convos = (ListView) findViewById(R.id.convList);
        convos.setAdapter(adapter);

        convos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String entry = (String) parent.getAdapter().getItem(position);
                Intent intent = new Intent(ScrollingActivity.this, ChatActivity.class);
                String contact = entry.split("\n")[0];
                Log.i("OUTPUT", "Conversation Entry: " + contact);
                intent.putExtra("CONTACT_NAME", contact);
                intent.putExtra("personal_key", getIntent().getStringExtra("personal_key"));
                startActivity(intent);
            }
        });
    }

    public boolean fileExists(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }
}