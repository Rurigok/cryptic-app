package net.cryptic.app;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */


abstract class Message {
    String type;
    String message;
    int deletionTimer;
    String flags; // no-additional-encryption, key-encryption, pattern-encryption, or both
}


class StoredMessage extends Message{

    String sentOrReceived;
    String dateStored;
    String firstRead;

    public StoredMessage(String string){
        this.type = "STORED";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.message = string;
        this.sentOrReceived = ChatActivity.storedOrReceived;
        this.deletionTimer = ChatActivity.deletionTimer;
        this.dateStored = sdf.format(new Date());
        this.firstRead = sdf.format(new Date());
        this.flags = ChatActivity.flags;
    }

}


class SentMessage extends Message{

    String nonceString;
    String dateSent;

    public SentMessage(String string){
        this.type = "SENT";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.message = string;
        this.nonceString = ChatActivity.nonceString;
        this.deletionTimer = ChatActivity.deletionTimer;
        this.dateSent = sdf.format(new Date());
        this.flags = ChatActivity.flags;
    }
}


public class JsonUtil {

    public static String toJSon(Message message) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        try {
            // Here we convert Java Object to JSON
            if(message.type.equals("STORED"){
                StoredMessage tmp = (StoredMessage) message;
                jsonObj.put("message", tmp.message); // Set the first name/pair
                jsonObj.put("sentOrReceived", tmp.sentOrReceived);
                jsonObj.put("deletionTimer", tmp.deletionTimer);
                jsonObj.put("dateStored", tmp.dateStored);
                jsonObj.put("firstRead", tmp.firstRead);
                jsonObj.put("flags", tmp.flags);
            }
            if(message.type.equals("SENT")){
                SentMessage tmp = (SentMessage) message;
                jsonObj.put("message", tmp.message); // Set the first name/pair
                jsonObj.put("nonceString", tmp.nonceString);
                jsonObj.put("deletionTimer", tmp.deletionTimer);
                jsonObj.put("dateSent", tmp.dateSent);
                jsonObj.put("flags", tmp.flags);
            }

            }
            else{
                Log.d("MESSAGE ERROR: ", "INCORRECT MESSAGE TYPE, MUST BE SENT/STORED");
            }


            return jsonObj.toString();

        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
