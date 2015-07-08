package cc.seeed.iot.ui_main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListRecyclerAdapter extends RecyclerView.Adapter<NodeListRecyclerAdapter.MainViewHolder> {
    private ArrayList<Node> nodes;

    public NodeListRecyclerAdapter(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);

        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        Node node = nodes.get(position);
        TextView tv_name = holder.tv_name;
        tv_name.setText(node.name);
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;

        public MainViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.txtvName);
        }
    }


//    public NodeListReyclerAdapter(Context context, ArrayList<Node> nodes) {
//        super(context, 0, nodes);
//
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Node node = getItem(position);
//        if (convertView == null) {
//            convertView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.main_list_item, parent, false);
//        }
//
//        TextView tv_name = (TextView) convertView.findViewById(R.id.txtvName);
//        tv_name.setText(node.name);
//
//        return convertView;
//
//    }
}
