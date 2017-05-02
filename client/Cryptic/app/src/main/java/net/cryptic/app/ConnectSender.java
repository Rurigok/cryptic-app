package net.cryptic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Sean on 5/2/2017.
 */

public class ConnectSender extends BroadcastReceiver {

    public ConnectSender() {
        super();
    }

    @Override
    public void onReceive(Context control, Intent intent) {
        String ip = intent.getStringExtra("SENDER_IP");
        String key = intent.getStringExtra("SENDER_KEY");

        // TODO: execute Diffie-Hellman key agreement

        try {
            Socket socket = new Socket(ip, 5677);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();

            // TODO: Decrypt

            JSONObject received = new JSONObject(message);

            // TODO: Encrypt with Personal Key

            // TODO: Write encrypted JSON to file
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
