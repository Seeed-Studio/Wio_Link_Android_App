package cc.seeed.iot.ui_main;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListRecyclerAdapter extends RecyclerView.Adapter<NodeListRecyclerAdapter.MainViewHolder> {
    private ArrayList<Node> nodes;
    private Context context;

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<String> mValues;

    public NodeListRecyclerAdapter(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        Node node = nodes.get(position);
        holder.tv_name.setText(node.name);

        if (node.online) {
            holder.mStausView.setImageResource(R.drawable.online_led);
        } else {
            holder.mStausView.setImageResource(R.drawable.offline_led);
        }
    }

    @Override
    public int getItemCount() {
        if (nodes == null)
            return 0;
        else
            return nodes.size();
    }

    public Node getItem(int position) {
        return nodes.get(position);
    }

    public Node removeItem(int position) {
        Node node = nodes.remove(position);
        notifyItemRemoved(position);
        return node;
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
        TextView tv_name;
        ImageView pop_menu;
        ImageView mStausView;
        View mView;

        NodeAction nodeAction;

        public MainViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tv_name = (TextView) itemView.findViewById(R.id.txtvName);
            pop_menu = (ImageView) itemView.findViewById(R.id.pop_menu);
            mStausView = (ImageView) itemView.findViewById(R.id.status_led);

            itemView.setOnClickListener(this);
            pop_menu.setOnClickListener(this);

            nodeAction = (NodeAction) mView.getContext();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.remove:
                    nodeAction.nodeRemove(getAdapterPosition());
                    return true;
                case R.id.detail:
                    nodeAction.nodeDetail(getAdapterPosition());
                    return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (v == pop_menu) {
                PopupMenu popupMenu = new PopupMenu(mView.getContext(), v);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.inflate(R.menu.ui_node_action);
                popupMenu.show();
            } else if (v == mView) {
                nodeAction.nodeSet(getAdapterPosition());
            }
        }
    }

    public interface NodeAction {
        public boolean nodeRemove(int position);
        public boolean nodeDetail(int position);
        public boolean nodeSet(int position);
    }

}
