package cc.seeed.iot.ui_main;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListRecyclerAdapter extends RecyclerSwipeAdapter<NodeListRecyclerAdapter.MainViewHolder> {
    private static final String TAG = "NodeListRecyclerAdapter";
    private ArrayList<Node> nodes;
    private Context context;

    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<String> mValues;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(View v, int position);
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public NodeListRecyclerAdapter(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);
        return new MainViewHolder(v, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        Node node = nodes.get(position);
        holder.mNameView.setText(node.name);

        if (node.online) {
            holder.mStatusView.setBackgroundColor(Color.GREEN);
        } else {
            holder.mStatusView.setBackgroundColor(Color.RED);
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

    public Node updateItem(int position, Node newNode) {
        nodes.set(position, newNode);
        notifyItemChanged(position);
        return newNode;
    }

    public boolean updateAll(ArrayList<Node> nodes) {
        this.nodes = nodes;
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return 0;
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private OnClickListener mOnClickListener;

        TextView mNameView;
        TextView mLocationView;
        ImageView mFavoriteView;
        ImageView mPopMenuView;
        View mStatusView;
        View mItemView;

        TextView mRenameView;
        TextView mDetailView;
        TextView mRemoveView;

        public MainViewHolder(View itemView, OnClickListener mOnClickListener) {
            super(itemView);
            this.mOnClickListener = mOnClickListener;

            mItemView = itemView;
            mNameView = (TextView) itemView.findViewById(R.id.name);
            mLocationView = (TextView) itemView.findViewById(R.id.location);
            mFavoriteView = (ImageView) itemView.findViewById(R.id.favorite);
            mPopMenuView = (ImageView) itemView.findViewById(R.id.dot);
            mStatusView = itemView.findViewById(R.id.status);

            mRenameView = (TextView) itemView.findViewById(R.id.rename);
            mDetailView = (TextView) itemView.findViewById(R.id.detail);
            mRemoveView = (TextView) itemView.findViewById(R.id.remove);

            mItemView.setOnClickListener(this);
            mLocationView.setOnClickListener(this);
            mFavoriteView.setOnClickListener(this);
            mPopMenuView.setOnClickListener(this);
            mRenameView.setOnClickListener(this);
            mDetailView.setOnClickListener(this);
            mRemoveView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v, getLayoutPosition());
            }

        }
    }
}
