package net.cryptic.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import android.widget.TextView;

import static net.cryptic.app.Storage.cookieManager;

/**
 * Created by Edward on 5/2/2017.
 */

public class ComposeMessage extends AppCompatActivity {
    TextView contactText;

    private EditText mEncryptView;
    private EditText mTimeoutView;
    private EditText mMessageView;
    CheckBox deleteCheck;
    //private EditText mTargetView;

    private final byte[] AAD_GCM = "CRYPTIC_MESSAGE".getBytes();
    private final int GCM_NONCE_LENGTH = 12;
    private final int GCM_TAG_LENGTH = 16;
    private final String PREFS_NAME = "CRYPTIC_DATA";

    private ComposeMessage.SendMessageTask mSendTask = null;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_message);

        deleteCheck = (CheckBox) findViewById(R.id.deleteBox);

        contactText = (TextView) findViewById(R.id.contactText);
        contactText.setText(getIntent().getStringExtra("CONTACT_NAME"));

        mEncryptView = (EditText) findViewById(R.id.encryptText);
        mTimeoutView = (EditText) findViewById(R.id.timeoutText);
        mMessageView = (EditText) findViewById(R.id.messageText);
        //mTargetView = (EditText) findViewById(R.id.toText);

        Button send = (Button) findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeSend();
                Intent intent = new Intent(ComposeMessage.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("CONTACT_NAME", getIntent().getStringExtra("CONTACT_NAME"));
                startActivity(intent);
                //attemptSend();
            }
        });
    }

    public void storeSend(){
        try {
            FileOutputStream outputStream = openFileOutput(getIntent().getStringExtra("CONTACT_NAME") + ".txt", Context.MODE_APPEND);
            JsonUtil jsonUtil = new JsonUtil();
            int timeout = 0;
            if(deleteCheck.isChecked())
                timeout = Integer.parseInt(mTimeoutView.getText().toString());
            StoredMessage storedMessage = new StoredMessage(mMessageView.getText().toString(), timeout);
            storedMessage.sentOrReceived = "SENT";
            outputStream.write(jsonUtil.toJSon(storedMessage).getBytes());
            outputStream.write("---separator---".getBytes());
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(JSONException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void attemptSend() {
        CheckBox deleteCheck = (CheckBox) findViewById(R.id.deleteBox);
        boolean delete = deleteCheck.isChecked();

        String text = mMessageView.getText().toString();
        //String target = mTargetView.getText().toString();
        String target = getIntent().getStringExtra("CONTACT_NAME");
        String encryptionString = mEncryptView.getText().toString();
        int timeout;

        if (delete)
            timeout = Integer.parseInt(mTimeoutView.getText().toString());
        else
            timeout = 0;

        Log.i("INPUT DATA", "TEXT: " + text + " TARGET: " + target + " KEY: " + encryptionString + " TIMEOUT: " + timeout);

        mSendTask = new ComposeMessage.SendMessageTask(this, text, target, encryptionString, timeout);
        mSendTask.execute((Void) null);
    }

    public String urlEncode(HashMap<String, String> form) {
        StringBuilder encoded = new StringBuilder();

        // URL encode the form key-value pairs. A trailing ampersand will be added which
        // shouldn't be a problem.
        try {
            for (String key : form.keySet()) {
                encoded.append(URLEncoder.encode(key, "UTF-8"));
                encoded.append("=");
                encoded.append(URLEncoder.encode(form.get(key), "UTF-8"));
                encoded.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encoded.toString();
    }

    public class SendMessageTask extends AsyncTask<Void, Void, Boolean> {

        private final ComposeMessage composeMessage;
        private final String mMessage;
        private final String mTarget;
        private final String mEncryption;
        private final int mTimeout;

        private String action = "";
        private boolean success = false;
        private String message = "";

        SendMessageTask(ComposeMessage composeMessage, String message, String target, String encryption, int timeout) {
            this.composeMessage = composeMessage;
            this.mMessage = message;
            this.mTarget = target;
            this.mEncryption = encryption;
            this.mTimeout = timeout;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            URL url = null;
            HttpURLConnection conn = null;
            String response;

            SharedPreferences settings = getApplication().getSharedPreferences(PREFS_NAME, 0);

            HashMap<String, String> form = new HashMap<>();

            form.put("username", settings.getString("username", null));
            form.put("target", mTarget);

            try{
                url = new URL("http://andrew.sanetra.me/cryptic/send-message");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie", TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
                conn.setDoOutput(true);

                OutputStream outputPost = new BufferedOutputStream(conn.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputPost, "UTF-8"));
                writer.write(urlEncode(form));
                writer.flush();
                writer.close();
                outputPost.close();
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String public_key = "";
            String device_ip = "";

            try {
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    response = "";
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    JSONObject jsonResponse = new JSONObject(response);
                    Log.i("JSON_RESPONSE", jsonResponse.toString());
                    //Removed action response from server
                    //action = jsonResponse.getString("action");
                    public_key = jsonResponse.getString("public_key");
                    device_ip = jsonResponse.getString("device_ip");
                }
                else {
                    Log.i("CRITICAL_ERROR", "Somewhere, something went very wrong.");
                }
            } catch (JSONException | IOException e) {
                Log.i("CRITICAL_ERROR", "Somewhere, something went very wrong.");
                e.printStackTrace();
            }

            Log.i("PRIVATE_KEY_GET", settings.getString("private_key", null));
            byte[] encrypted_private = Base64.decode(settings.getString("private_key", null), Base64.DEFAULT);
            byte[] public_bytes = Base64.decode(public_key, Base64.DEFAULT);

            String pKey = getIntent().getStringExtra("personal_key");
            byte[] personal_key = Base64.decode(pKey, Base64.DEFAULT);
            SecretKeySpec keyspec = new SecretKeySpec(personal_key, "AES");

            String encrypted_message = "";
            String GCM_NONCE = "";

            try {

                // TODO: encrypt and store message on device

                //Random generator for GCM nonce
                SecureRandom random = new SecureRandom();
                final byte[] nonce = new byte[GCM_NONCE_LENGTH];
                random.nextBytes(nonce);
                GCM_NONCE = new String(nonce);

                //Create encryption cipher
                Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
                byte[] decryption_nonce = settings.getString("decryption_nonce", null).getBytes();
                Log.i("NONCE", settings.getString("decryption_nonce", null));
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, decryption_nonce);
                c.init(Cipher.DECRYPT_MODE, keyspec, spec);
                c.update(AAD_GCM);
                byte[] decrypted_private = c.doFinal(encrypted_private);

                KeyFactory kf = KeyFactory.getInstance("ECDH", "SC");

                X509EncodedKeySpec x509ks = new X509EncodedKeySpec(Base64.decode(public_bytes, Base64.DEFAULT));
                PublicKey pubKey = kf.generatePublic(x509ks);
                PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(decrypted_private);
                PrivateKey privKey = kf.generatePrivate(p8ks);

                KeyAgreement KA = KeyAgreement.getInstance("ECDH", "SC");
                KA.init(privKey);
                KA.doPhase(pubKey, true);

                SecretKey sharedKey = KA.generateSecret("ECDH");

                MessageDigest keyDigest = MessageDigest.getInstance("SHA-512/256");
                keyDigest.update(sharedKey.getEncoded());
                byte[] EncryptionKey = keyDigest.digest();

                byte[] plaintext_bytes = mMessage.getBytes();

                SecretKeySpec EncryptionSpec = new SecretKeySpec(EncryptionKey, "AES");

                Cipher EncryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec GCMEncrypt = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
                EncryptionCipher.init(Cipher.DECRYPT_MODE, EncryptionSpec, GCMEncrypt);
                EncryptionCipher.updateAAD(AAD_GCM);
                byte[] encrypted_bytes = EncryptionCipher.doFinal(plaintext_bytes);

                encrypted_message = new String(encrypted_bytes);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                    InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
                    | InvalidKeySpecException | NoSuchProviderException e) {
                e.printStackTrace();
            }

            String nonceString = GCM_NONCE;
            SentMessage message = new SentMessage(encrypted_message, nonceString, mTimeout);

            String payload = "";

            try {
                payload = JsonUtil.toJSon(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            int port = 5678;

            try {
                ServerSocket socket = new ServerSocket(port);
                Socket sock = socket.accept();

                // Quit if fraudulent IP tries to connect
                    /*if (!sock.getInetAddress().getHostAddress().equals(device_ip)) {
                        sock.close();
                        socket.close();
                    }*/

                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                out.write(payload);
                out.close();
                sock.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(ComposeMessage.this, ChatActivity.class);
            intent.putExtra("CONTACT_NAME", mTarget);
            intent.putExtra("personal_key", new String(personal_key));
            startActivity(intent);

            return true;
        }
    }
}