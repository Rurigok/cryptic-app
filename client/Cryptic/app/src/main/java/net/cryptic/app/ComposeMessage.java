package net.cryptic.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Edward on 5/2/2017.
 */

public class ComposeMessage extends AppCompatActivity {
    TextView textView;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_message);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("To: " + getIntent().getStringExtra("CONTACT_NAME"));
    }

}
