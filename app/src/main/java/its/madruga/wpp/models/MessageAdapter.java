package its.madruga.wpp.models;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import its.madruga.wpp.core.databases.MessageHistory;
import its.madruga.wpp.xposed.plugins.core.DesignUtils;
import its.madruga.wpp.xposed.plugins.core.Utils;

public class MessageAdapter extends ArrayAdapter {
    private Context context;
    private List<MessageHistory.MessageItem> items;

    public MessageAdapter(Context context, List<MessageHistory.MessageItem> items) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view1 = super.getView(position, convertView, parent);
        TextView textView0 = view1.findViewById(android.R.id.text1);
        textView0.setTextSize(14.0f);
        textView0.setTextColor(DesignUtils.getPrimaryTextColor(view1.getContext()));
        textView0.setText(this.items.get(position).message);
        TextView textView1 = view1.findViewById(android.R.id.text2);
        textView1.setTextSize(12.0f);
        textView1.setAlpha(0.75f);
        textView1.setTypeface(null, Typeface.ITALIC);
        textView1.setTextColor(DesignUtils.getPrimaryTextColor(view1.getContext()));
        var timestamp = this.items.get(position).timestamp;
        textView1.setText((timestamp == 0L ? "Message Original" : "✏️ " + Utils.getDateTimeFromMillis(timestamp)));
        return view1;
    }

}