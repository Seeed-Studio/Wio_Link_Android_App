package cc.seeed.iot.ui_main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.Node;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListRecyclerAdapter extends RecyclerSwipeAdapter<NodeListRecyclerAdapter.MainViewHolder> {
    private static final String TAG = "NodeListRecyclerAdapter";
    private List<Node> nodes = new ArrayList<>();
    private Context context;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(View v, int position);
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public NodeListRecyclerAdapter(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_node, parent, false);
        return new MainViewHolder(v, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        Node node = nodes.get(position);
        holder.mNameView.setText(node.name);
        holder.mSwipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);

        if (node.board.equals(Constant.WIO_LINK_V1_0)) {
            holder.mBoardView.setImageResource(R.drawable.link_small);
        } else if (node.board.equals(Constant.WIO_NODE_V1_0)) {
            holder.mBoardView.setImageResource(R.drawable.node_small);
        }

        if (node.dataxserver == null || node.dataxserver.equals(Common.OTA_INTERNATIONAL_IP)
                || node.dataxserver.equals(Common.OTA_CHINA_IP))
            holder.mXserverView.setText("");
        else
            holder.mXserverView.setText(node.dataxserver);

        if (node.online) {
            holder.mStatusView.setBackgroundResource(R.color.online);
            holder.mOnlineLedView.setImageResource(R.drawable.online_led);
            holder.mOnlineView.setText(R.string.online);
            holder.mRenameView.setBackgroundResource(R.color.online);
            holder.mDetailView.setBackgroundResource(R.color.online);
            holder.mRemoveView.setBackgroundResource(R.color.online);
        } else {
            holder.mStatusView.setBackgroundResource(R.color.offline);
            holder.mOnlineLedView.setImageResource(R.drawable.offline_led);
            holder.mOnlineView.setText(R.string.offline);
            holder.mRenameView.setBackgroundResource(R.color.offline);
            holder.mDetailView.setBackgroundResource(R.color.offline);
            holder.mRemoveView.setBackgroundResource(R.color.offline);
        }

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (int i = 0; i < 4; i++) {
            try {
                holder.mGroveViews.get(i).setVisibility(View.VISIBLE);
                PinConfig pinConfig = pinConfigs.get(i); //IndexOutOfBoundsException
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL; //maybe null
                UrlImageViewHelper.setUrlDrawable(holder.mGroveViews.get(i), url, R.drawable.grove_no,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
                holder.mGroveViews.get(i).setVisibility(View.GONE);
            }
        }

        if (pinConfigs.size() > 4) {
            Integer over_num = pinConfigs.size() - 4;
            holder.mGroveOverView.setVisibility(View.VISIBLE);
            holder.mGroveOverView.setText("+" + String.valueOf(over_num));
        } else {
            holder.mGroveOverView.setVisibility(View.GONE);
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

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public Node updateItem(int position) {
        Node node = nodes.get(position);
        notifyItemChanged(position);
        return node;
    }

    public boolean updateAll(List<Node> nodes) {
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

        ImageView mBoardView;
        TextView mNameView;
        TextView mOnlineView;
        ImageView mOnlineLedView;
        TextView mXserverView;
        ImageView mFavoriteView;
        ImageView mPopMenuView;
        View mStatusView;
        View mItemView;

        TextView mRenameView;
        TextView mDetailView;
        TextView mRemoveView;

        List<ImageView> mGroveViews;
        TextView mGroveOverView;

        SwipeLayout mSwipeLayout;

        public MainViewHolder(View itemView, OnClickListener mOnClickListener) {
            super(itemView);
            this.mOnClickListener = mOnClickListener;

            mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe_layout);
            mItemView = itemView;
            mBoardView = (ImageView) itemView.findViewById(R.id.board_img);
            mNameView = (TextView) itemView.findViewById(R.id.name);
            mOnlineView = (TextView) itemView.findViewById(R.id.online);
            mOnlineLedView = (ImageView) itemView.findViewById(R.id.online_led);
            mXserverView = (TextView) itemView.findViewById(R.id.xserver_ip);
//            mFavoriteView = (ImageView) itemView.findViewById(R.id.favorite);
//            mPopMenuView = (ImageView) itemView.findViewById(R.id.dot);
            mStatusView = itemView.findViewById(R.id.status);

            mRenameView = (TextView) itemView.findViewById(R.id.setting);
            mDetailView = (TextView) itemView.findViewById(R.id.api);
            mRemoveView = (TextView) itemView.findViewById(R.id.remove);

            mGroveViews = new ArrayList<>();
            mGroveViews.add(0, (CircleImageView) itemView.findViewById(R.id.grove_image_1));
            mGroveViews.add(1, (CircleImageView) itemView.findViewById(R.id.grove_image_2));
            mGroveViews.add(2, (CircleImageView) itemView.findViewById(R.id.grove_image_3));
            mGroveViews.add(3, (CircleImageView) itemView.findViewById(R.id.grove_image_4));
            mGroveOverView = (TextView) itemView.findViewById(R.id.grove_over);

            mItemView.setOnClickListener(this);
//            mLocationView.setOnClickListener(this);
//            mFavoriteView.setOnClickListener(this);
//            mPopMenuView.setOnClickListener(this);
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
