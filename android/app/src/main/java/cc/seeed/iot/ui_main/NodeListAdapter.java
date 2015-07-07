package cc.seeed.iot.ui_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListAdapter extends ArrayAdapter<Node> {
    public NodeListAdapter(Context context, ArrayList<Node> nodes) {
        super(context, 0, nodes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Node node = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_list_item, parent, false);
        }

//        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
//        TextView tv_sn = (TextView) convertView.findViewById(R.id.tv_node_sn);
//        TextView tv_key = (TextView) convertView.findViewById(R.id.tv_node_key);
//        tv_name.setText("name:" + node.name);
//        tv_sn.setText("node_sn:" + node.node_sn);
//        tv_key.setText("node_key:" + node.node_key);

        return convertView;

    }
}
