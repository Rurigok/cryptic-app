package net.cryptic.app;

import java.util.Date;

/**
 * Created by Edward on 4/20/2017.
 */

public class Conversation {
    private String fromUser;
    private Date date;

    public Conversation(String fromUser, Date date)
    {
        this.fromUser = fromUser + "\n Last Message ";
        this.date = date;
        long diff = Math.abs(date.getTime() - (new Date()).getTime());
        int diffSec = (int)(diff/1000);
        if(diffSec < 60)
            this.fromUser += "seconds ago";
        else if((diffSec > 60) && (diffSec < (60 * 60)))
            this.fromUser += "over a minute ago";
        else if((diffSec > (60 * 60)) && (diffSec < (60 * 60 * 24)))
            this.fromUser += "over an hour ago";
        else if((diffSec > (60 * 60 * 24)) && (diffSec < (60 * 60 * 24 * 7)))
            this.fromUser += "over a day ago";
        else
            this.fromUser += "long long ago...";

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
