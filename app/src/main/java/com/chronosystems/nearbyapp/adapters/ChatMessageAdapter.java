package com.chronosystems.nearbyapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chronosystems.nearbyapp.R;
import com.chronosystems.nearbyapp.domain.messages.ChatMessage;
import com.chronosystems.nearbyapp.utils.AppConstants;

import java.util.Date;
import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private String mTimeColorYou;
    private String mTimeColorFrom;

    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessageList) {
        super(context, 0, chatMessageList);

        //noinspection ResourceType
        mTimeColorYou = "#" + context.getResources().getString(R.color.bg_msg_time_you).substring(3);
        //noinspection ResourceType
        mTimeColorFrom = "#" + context.getResources().getString(R.color.bg_msg_time_from).substring(3);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /**
         * The following list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         * */
        final ChatMessage m = getItem(position);
        final LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Identifying the message owner
        if (m.isSelf()) {
            // message belongs to you, so load the right aligned layout
            convertView = mInflater.inflate(R.layout.item_chat_message_right, null);
        } else {
            // message belongs to other person, load the left aligned layout
            convertView = mInflater.inflate(R.layout.item_chat_message_left, null);
        }

        // format and write the message
        writeMessage(position, convertView, m);

        return convertView;
    }

    private void writeMessage(int position, final View convertView, final ChatMessage m) {
        final TextView lblFrom = (TextView) convertView.findViewById(R.id.tv_from);
        final TextView txtMsg = (TextView) convertView.findViewById(R.id.tv_message);

        final String from = m.getUserName();

        if (position == 0) {
            m.setNewSender(true);
        } else if (position > 0 && m.isNewSender() == null) {
            final ChatMessage last = getItem(position - 1);
            m.setNewSender(!from.equals(last.getUserName()));
        }

        if (Boolean.TRUE.equals(m.isNewSender())) {
            lblFrom.setText(from);
        } else {
            lblFrom.setVisibility(View.GONE);
        }

        txtMsg.setText(Html.fromHtml(
                m.getText() + String.format("  <font color='%s'><sub><small>%s</small></sub></font>",
                        m.isSelf() ? mTimeColorYou : mTimeColorFrom,
                        getCurrentTime())));
    }

    private String getCurrentTime() {
        return AppConstants.MESSAGE_TIME_FORMAT.format(new Date()).replace(":", "h");
    }
}