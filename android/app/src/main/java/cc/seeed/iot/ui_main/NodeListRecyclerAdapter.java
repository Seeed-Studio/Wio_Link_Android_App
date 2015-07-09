package cc.seeed.iot.ui_main;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

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
    public void onBindViewHolder(MainViewHolder holder, int position) {
        Node node = nodes.get(position);
        TextView tv_name = holder.tv_name;
        ImageButton pop_menu = holder.pop_menu;
        tv_name.setText(node.name);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("iot", "hha");
            }
        });

    }

    @Override
    public int getItemCount() {
        if (nodes == null)
            return 0;
        else
            return nodes.size();
    }


    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        ImageButton pop_menu;
        View mView;

        public MainViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tv_name = (TextView) itemView.findViewById(R.id.txtvName);
            pop_menu = (ImageButton) itemView.findViewById(R.id.pop_menu);

//            itemView.setOnClickListener(this);
//            pop_menu.setOnClickListener(this);

        }

    }
}
