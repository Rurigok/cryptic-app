package net.cryptic.app;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Edward on 4/20/2017.
 */

public class ConversationAdapter extends BaseAdapter {

    private List<Conversation> conversations;
    private Activity context;

    public ConversationAdapter(Activity context, List<Conversation> conversations)
    {
        this.context = context;
        this.conversations = conversations;
    }

    @Override
    public int getCount() {
        if (conversations != null) {
            return conversations.size();
        } else {
            return 0;
        }
    }

    @Override
    public Conversation getItem(int position) {
        if (conversations != null) {
            return conversations.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(Conversation message) {
        conversations.add(message);
    }

    public void add(List<Conversation> messages) {
        conversations.addAll(messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }
}
