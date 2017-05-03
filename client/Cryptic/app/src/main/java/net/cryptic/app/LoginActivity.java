package net.cryptic.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import static net.cryptic.app.Storage.cookieManager;

/**
 * A login screen that offers login via user/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private final byte[] AAD_GCM = "CRYPTIC_MESSAGE".getBytes();
    private final int GCM_NONCE_LENGTH = 12;
    private final int GCM_TAG_LENGTH = 16;
    private final String PREFS_NAME = "CRYPTIC_DATA";
    private String personal_key;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getApplication().getSharedPreferences(PREFS_NAME, 0);

        // Attempt to login with cookies instead of user input credentials
        if (settings.getString("cookie", null) != null) {
            mAuthTask = new UserLoginTask(this, "cookie", "cookie");
            mAuthTask.execute((Void) null);
        }

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.user);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid user, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final LoginActivity loginActivity;
        private final String mUsername;
        private final String mPassword;

        private String action = "";
        private boolean success = false;
        private String message = "";

        UserLoginTask(LoginActivity loginActivity, String user, String password) {
            this.loginActivity = loginActivity;
            this.mUsername = user;
            this.mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            URL url = null;
            HttpURLConnection conn = null;
            String response;
            boolean newkey = false;

            HashMap<String, String> form = new HashMap<>();

            String selfIP = Utils.getIPAddress(true);
            form.put("device_ip", selfIP);

            SharedPreferences settings = getApplication().getSharedPreferences(PREFS_NAME, 0);

            String cookie = settings.getString("cookie", null);

            String private_key = settings.getString("private_key", null);

            if (/*private_key == null*/ true) {
                newkey = true;

                //Initialize curve to a NIST standard
                ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp224k1");

                try {
                    //Create Key Generator
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
                    kpg.initialize(ecParamSpec);

                    //Create keypair
                    KeyPair kp = kpg.generateKeyPair();

                    //Store Keys as Strings
                    String pubStr = Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT);
                    String privStr = Base64.encodeToString(kp.getPrivate().getEncoded(), Base64.DEFAULT);

                    KeyFactory kf = KeyFactory.getInstance("ECDH", "SC");

                    //Encode Keys
                    X509EncodedKeySpec x509ks = new X509EncodedKeySpec(Base64.decode(pubStr, Base64.DEFAULT));
                    PublicKey pubKey = kf.generatePublic(x509ks);
                    PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(Base64.decode(privStr, Base64.DEFAULT));
                    PrivateKey privKey = kf.generatePrivate(p8ks);

                    String public_key = Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT);
                    Log.i("PUBLIC_KEY", public_key);
                    private_key = Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT);
                    form.put("public_key", public_key);
                } catch (InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
            else
                form.put("public_key", "PLACEHOLDER_KEY_IGNORE");

            form.put("username", mUsername);
            form.put("password", mPassword);

            // TODO: https functionality
            try{
                url = new URL("http://andrew.sanetra.me/cryptic/login");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                if (cookie != null) {
                    conn.setRequestProperty("Cookie", TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
                    Log.i("OUTPUT", "Cookie sent: " + cookie);
                }

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

            // TODO: responseCode nonsense
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
                    success = jsonResponse.getBoolean("success");
                    personal_key = null;
                    try {
                        personal_key = jsonResponse.getString("personal_key");
                        Log.i("PERSONAL_KEY_LOGIN", personal_key);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("JSON ERROR", "Andrew probably hasn't fixed things yet.");
                    }

                    SharedPreferences.Editor editor = settings.edit();
                    cookie = Storage.setCookies(conn.getHeaderFields());
                    editor.putString("cookie", cookie);
                    editor.putString("username", mUsername);

                    if (newkey && personal_key != null) {
                        byte[] privkey = Base64.decode(private_key, Base64.DEFAULT);
                        byte[] perskey = Base64.decode(personal_key, Base64.DEFAULT);
                        SecretKeySpec Encryption_Spec = new SecretKeySpec(perskey, "AES");

                        //Random generator for GCM nonce
                        SecureRandom random = new SecureRandom();

                        //Create encryption cipher
                        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
                        final byte[] nonce = new byte[GCM_NONCE_LENGTH];
                        random.nextBytes(nonce);
                        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
                        c.init(Cipher.ENCRYPT_MODE, Encryption_Spec, spec);
                        //c.update(AAD_GCM);
                        Log.i("ENCRYPTION_CIPHER_HASH", c.hashCode()+"");

                        String GCM_NONCE = Base64.encodeToString(nonce, Base64.DEFAULT);
                        editor.putString("decryption_nonce", GCM_NONCE); //horribly insecure and defeats the point
                        Log.i("NONCE SET", GCM_NONCE);

                        byte[] Encrypted_Private_Key = c.doFinal(privkey);
                        String Secure_Key = Base64.encodeToString(Encrypted_Private_Key, Base64.DEFAULT);
                        editor.putString("private_key", Secure_Key);
                        Log.i("PRIVATE_KEY_SET", Secure_Key);
                        Log.i("AAD_LOGIN", new String(AAD_GCM));
                    }

                    editor.apply();
                    Log.i("OUTPUT", "Cookie stored" + cookie);

                    if (success) {
                        Intent mServiceIntent = new Intent(loginActivity, ServerListener.class);
                        mServiceIntent.putExtra("personal_key", personal_key);
                        startService(mServiceIntent);
                    }

                    if (!success && !(mUsername.equals("cookie") && mPassword.equals("cookie")))
                        message = jsonResponse.getString("message");
                }
                else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: register the new account here
            return success;
        }

        /**
         * Handles UI changes after login request is made and completed.
         * @param success true if login was successful, false otherwise
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(loginActivity, ScrollingActivity.class);
                intent.putExtra("personal_key", personal_key);
                startActivity(intent);
            }
            else {
                if (!message.isEmpty()) {
                    mPasswordView.setError(message);
                    mPasswordView.requestFocus();
                }
                else if (!(mUsername.equals("cookie") && mPassword.equals("cookie"))) {
                    mPasswordView.setError("Something went wrong. Please try again");
                    mPasswordView.requestFocus();
                }
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
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

    }
}