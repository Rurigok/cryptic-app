package net.cryptic.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Sean on 5/2/2017.
 */

public class ServerListener extends IntentService {

    private final String PREFS_NAME = "CRYPTIC_DATA";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ServerListener(String name) {
        super(name);
    }

    public ServerListener() {
        super("ServerListener");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences settings = getApplication().getSharedPreferences(PREFS_NAME, 0);

        int port = 5677;
        String personal_key = intent.getStringExtra("personal_key");
        String private_key = settings.getString("private_key", null);
        byte[] nonce = settings.getString("decryption_nonce", null).getBytes();

        try {
            ServerSocket socket = new ServerSocket(port);
            Socket sock = socket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String message = in.readLine();
            JSONObject payload = new JSONObject(message);

            String sender_ip = payload.getString("sender_ip");
            String sender_key = payload.getString("sender_public_key");

            Intent localIntent = new Intent("MESSAGE_RECEIVED");
            localIntent.putExtra("SENDER_IP", sender_ip);
            localIntent.putExtra("SENDER_KEY", sender_key);
            localIntent.putExtra("personal_key", personal_key);
            localIntent.putExtra("private_key", private_key.getBytes());
            localIntent.putExtra("nonce", nonce);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}