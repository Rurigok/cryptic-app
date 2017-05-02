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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

                startActivity(new Intent(ScrollingActivity.this, LoginActivity.class));
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

        conversations.add(new Conversation("Andrew", date));
        conversations.add(new Conversation("Edward", date2));
        conversations.add(new Conversation("Sean", date3));

        FileWriter out;
        for(Conversation c : conversations)
        {
            stringList.add(c.getDisplay());
            Log.i("Inform me", "NOW!");
            try {
                Log.i("Inform me", Environment.getExternalStorageDirectory().toString());
                File file = new File(Environment.getExternalStorageDirectory(), c.getFromUser() + ".txt");
                out = new FileWriter(file);
                //out.append(c.getFromUser());
                out.write(c.getFromUser());
                out.flush();
                out.close();

                Log.i("Inform me", "YO!");
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(IOException e){
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
                startActivity(intent);
            }
        });
    }
}