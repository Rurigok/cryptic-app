package net.cryptic.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.cryptic.app.R.layout.activity_scrolling;

public class ScrollingActivity extends AppCompatActivity {

    List<Conversation> conversations = new ArrayList<>();
    ListView messages;
    ConversationAdapter adapter;
    List<String> stringList = new ArrayList<>();
    public static final ScrollingActivity scrollingActivity = new ScrollingActivity();

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

                startActivity(new Intent(ScrollingActivity.this, LoginActivity.class));
            }
        });

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        Date date2 = new Date();
        Date date3 = new Date();
        try {
            date2 = format.parse("2017/04/20 12:24:34");
            date3 = format.parse("2016/04/19 12:24:34");
        } catch (ParseException e){
            e.printStackTrace();
        }
        conversations.add(new Conversation("Andrew", date));
        conversations.add(new Conversation("Edward", date2));
        conversations.add(new Conversation("Sean", date3));

        for(Conversation c : conversations)
        {
            stringList.add(c.getFromUser());
            Log.d("OUTPUT", c.getFromUser());
            Log.d("OUTPUT", c.getDate().toString());
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.listview, stringList);

        messages = (ListView) findViewById(R.id.convList);
        messages.setAdapter(adapter);
    }
}
