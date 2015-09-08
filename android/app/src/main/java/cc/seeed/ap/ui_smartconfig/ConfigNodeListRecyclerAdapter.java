package cc.seeed.ap.ui_smartconfig;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cc.seeed.ap.udp.ConfigNodeData;
import cc.seeed.ap.ui_setnode.GroveFilterRecyclerAdapter;
import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/25.
 */
public class ConfigNodeListRecyclerAdapter extends RecyclerView.Adapter<ConfigNodeListRecyclerAdapter.MainViewHolder> {
    private ArrayList<ConfigNodeData> localNodes;
    private Context context;

    SparseBooleanArray selector;
    private GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener mItemClickListener;

    public ConfigNodeListRecyclerAdapter(ArrayList<ConfigNodeData> localNodes) {
        this.localNodes = localNodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.config_node_list_item, parent, false);

        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        final ConfigNodeData localNode = localNodes.get(position);
        holder.mMacView.setText(localNode.mac);
        holder.mIpView.setText(localNode.ip);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Todo:set node" + position, Snackbar.LENGTH_SHORT).show();

                Intent intent = new Intent(context, SetNodeNameActivity.class);
                intent.putExtra(SetNodeNameActivity.NODE_LOCAL_IP_ADDRESS, localNode.ip);
                context.startActivity(intent);
            }
        });
    }

    public void selectItem(int position) {
        notifyDataSetChanged();
    }


    public void clearSelectItem() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return localNodes.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView mMacView;
        TextView mIpView;
        View mView;

        public MainViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mMacView = (TextView) itemView.findViewById(R.id.txtvmac);
            mIpView = (TextView) itemView.findViewById(R.id.txtvip);

        }

    }

}
