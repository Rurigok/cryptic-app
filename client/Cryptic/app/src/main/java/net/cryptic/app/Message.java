package net.cryptic.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.cryptic.app.ChatActivity.jsonUtil;

/**
 *
 */


abstract class Message {

    String from;
    String type;
    String message;
    int deletionTimer;
    String flags; // no-additional-encryption, key-encryption, pattern-encryption, or both
}


class StoredMessage extends Message{

    String sentOrReceived;
    String dateStored;
    String firstRead;

    public StoredMessage(String string, int timeout){
        this.type = "STORED";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.message = string;
        this.sentOrReceived = "RECEIVED";
        this.deletionTimer = timeout;
        this.dateStored = sdf.format(new Date());
        this.firstRead = sdf.format(new Date());
        this.flags = ChatActivity.flags;
    }

    public void storeMessage(StoredMessage msg, AppCompatActivity activity){
        msg.sentOrReceived = "SENT";
        msg.type = "STORED";
        Log.i("Output", "SENDING MESSAGE:" + message);
        //String str = message.toString();
        FileOutputStream outputStream;
        try {
            outputStream = activity.openFileOutput(msg.from + ".txt", Context.MODE_PRIVATE);
            outputStream.write(jsonUtil.toJSon(msg).getBytes());
            Log.i("STORED IN", activity.getFilesDir().getAbsolutePath().toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}


class SentMessage extends Message{
    String nonceString;
    String dateSent;

    public SentMessage(String string, String nonce, int timeout){
        this.type = "SENT";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.message = string;
        this.nonceString = nonce;
        this.deletionTimer = timeout;
        this.dateSent = sdf.format(new Date());
        this.flags = ChatActivity.flags;
    }
}


class JsonUtil {

    public static String toJSon(Message message) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        Log.i("TYPE", message.type);
        message.type.trim();
        try {
            // Here we convert Java Object to JSON
            //if("STORED".equals(message.type)) {
                StoredMessage tmp = (StoredMessage) message;
                jsonObj.put("message", tmp.message); // Set the first name/pair
                jsonObj.put("sentOrReceived", tmp.sentOrReceived);
                jsonObj.put("deletionTimer", tmp.deletionTimer);
                jsonObj.put("dateStored", tmp.dateStored);
                jsonObj.put("firstRead", tmp.firstRead);
                jsonObj.put("flags", tmp.flags);
            //}
            /*if("SENT".equals(message.type)) {
                SentMessage tmp = (SentMessage) message;
                jsonObj.put("message", tmp.message); // Set the first name/pair
                jsonObj.put("nonceString", tmp.nonceString);
                jsonObj.put("deletionTimer", tmp.deletionTimer);
                jsonObj.put("dateSent", tmp.dateSent);
                jsonObj.put("flags", tmp.flags);
            }
            else{
                Log.d("MESSAGE ERROR", "INCORRECT MESSAGE TYPE, MUST BE SENT/STORED");
            }*/


            return jsonObj.toString();

        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}