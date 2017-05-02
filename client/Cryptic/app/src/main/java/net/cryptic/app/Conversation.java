package net.cryptic.app;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation {
    private String fromUser;
    private Date date;
    private String display;
    private List<JSONObject> messages = new ArrayList<JSONObject>();

    public Conversation(String fromUser, Date date)
    {
        this.fromUser = fromUser;
        this.display = fromUser + "\nLast Message ";
        this.date = date;
        long diff = Math.abs(date.getTime() - (new Date()).getTime());
        int diffSec = (int)(diff/1000);
        if(diffSec < 60)
            display += "seconds ago";
        else if((diffSec > 60) && (diffSec < (60 * 60)))
            display += "over a minute ago";
        else if((diffSec > (60 * 60)) && (diffSec < (60 * 60 * 24)))
            display += "over an hour ago";
        else if((diffSec > (60 * 60 * 24)) && (diffSec < (60 * 60 * 24 * 7)))
            display += "over a day ago";
        else
            display += "long long ago...";
        JSONObject js = new JSONObject();
        try {
            js.put("From", "Andrew");
            js.put("Message", "LoL is bad.");
        } catch (JSONException e){
            e.printStackTrace();
        }
        messages.add(js);
    }

    public boolean storeMessages(){
        //DOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO IIIIIIIIIIITTTTTTTTTTTTTTTTTTTTTTTTTTTT
        return true;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
