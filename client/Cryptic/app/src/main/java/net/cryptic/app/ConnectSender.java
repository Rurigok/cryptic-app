package net.cryptic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Sean on 5/2/2017.
 */

public class ConnectSender extends BroadcastReceiver {

    private final byte[] AAD_GCM = "CRYPTIC_MESSAGE".getBytes();
    private final int GCM_TAG_LENGTH = 16;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public ConnectSender() {
        super();
    }

    @Override
    public void onReceive(Context control, Intent intent) {
        String ip = intent.getStringExtra("SENDER_IP");
        String key = intent.getStringExtra("SENDER_KEY");
        String personal_key = intent.getStringExtra("personal_key");

        byte[] perskey = Base64.decode(personal_key, Base64.DEFAULT);
        SecretKeySpec keyspec = new SecretKeySpec(perskey, "AES");

        byte[] nonce = intent.getByteArrayExtra("nonce");

        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            c.init(Cipher.DECRYPT_MODE, keyspec, spec);
            c.updateAAD(AAD_GCM);
            byte[] private_key = c.doFinal(intent.getByteArrayExtra("private_key"));

            KeyFactory kf = KeyFactory.getInstance("ECDH", "SC");

            X509EncodedKeySpec x509ks = new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT));
            PublicKey pubKey = kf.generatePublic(x509ks);
            PKCS8EncodedKeySpec p8ks = new PKCS8EncodedKeySpec(private_key);
            PrivateKey privKey = kf.generatePrivate(p8ks);

            KeyAgreement KA = KeyAgreement.getInstance("ECDH", "SC");
            KA.init(privKey);
            KA.doPhase(pubKey, true);

            SecretKey sharedKey = KA.generateSecret("ECDH");

            Socket socket = new Socket(ip, 5677);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();

            MessageDigest keyDigest = MessageDigest.getInstance("SHA-512/256");
            keyDigest.update(sharedKey.getEncoded());
            byte[] DecryptionKey = keyDigest.digest();

            JSONObject received = new JSONObject(message);

            String encrypted_message = received.getString("message");
            byte[] encrypted_bytes = encrypted_message.getBytes();
            String shared_nonce = received.getString("nonce");
            byte[] decryption_nonce = shared_nonce.getBytes();

            SecretKeySpec DecryptionSpec = new SecretKeySpec(DecryptionKey, "AES");

            Cipher DecryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec GCMDecrypt = new GCMParameterSpec(GCM_TAG_LENGTH * 8, decryption_nonce);
            DecryptionCipher.init(Cipher.DECRYPT_MODE, DecryptionSpec, GCMDecrypt);
            DecryptionCipher.updateAAD(AAD_GCM);
            byte[] decrypted_bytes = DecryptionCipher.doFinal(encrypted_bytes);

            Cipher EncryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec GCMEncrypt = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            EncryptionCipher.init(Cipher.ENCRYPT_MODE, keyspec, GCMEncrypt);
            EncryptionCipher.updateAAD(AAD_GCM);
            byte[] recrypted_bytes = EncryptionCipher.doFinal(decrypted_bytes);

            // TODO: Write encrypted JSON to file
            //received.get("");
        } catch (JSONException | IOException | NoSuchPaddingException | NoSuchAlgorithmException |
                NoSuchProviderException | InvalidAlgorithmParameterException |InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}