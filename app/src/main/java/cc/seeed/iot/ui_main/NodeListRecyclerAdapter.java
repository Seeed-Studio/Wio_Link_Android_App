package cc.seeed.iot.ui_main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.view.FontTextView;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node1, parent, false);
        return new MainViewHolder(v, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        Node node = nodes.get(position);

        if (node.board == null) {
            node.board = Constant.WIO_LINK_V1_0;
        }
        switch (node.board) {
            default:
            case Constant.WIO_LINK_V1_0:
                holder.mSDVNode.setImageResource(R.drawable.link_small);
                break;
            case Constant.WIO_NODE_V1_0:
                holder.mSDVNode.setImageResource(R.drawable.node_small);
                break;
        }
        holder.mTvTitle.setText(TextUtils.isEmpty(node.name)?"Wio Link"+position+1:node.name);
        if (node.online){
            holder.mTvState.setText(R.string.online);
            holder.mTvState.setBackgroundResource(R.drawable.online_state_bg);
        }else {
            holder.mTvState.setText(R.string.offline);
            holder.mTvState.setBackgroundResource(R.drawable.offline_state_bg);
        }


     /*   if (node.dataxserver == null || node.dataxserver.equals(CommonUrl.OTA_SERVER_IP)|| node.dataxserver.equals(CommonUrl.OTA_SERVER_IP))
            holder.mXserverView.setText("");
        else
            holder.mXserverView.setText(node.dataxserver);*/

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (int i = 0; i < 4; i++) {
            try {
                holder.mGroveViews.get(i).setVisibility(View.VISIBLE);
                PinConfig pinConfig = pinConfigs.get(i); //IndexOutOfBoundsException
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL; //maybe null
                ImgUtil.displayImg(holder.mGroveViews.get(i),url,R.drawable.link_small);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
                holder.mGroveViews.get(i).setVisibility(View.GONE);
            }
        }

        holder.mTvConnectedNum.setText(""+pinConfigs.size());
        if (pinConfigs.size() > 4) {
            Integer over_num = pinConfigs.size() - 4;
            holder.mTvMoreGroveNum.setVisibility(View.VISIBLE);
            holder.mTvMoreGroveNum.setText("+"+over_num);
        } else {
            holder.mTvMoreGroveNum.setVisibility(View.GONE);
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

    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnClickListener mOnClickListener;

        private SimpleDraweeView mSDVNode;
        private FontTextView mTvTitle;
        private FontTextView mTvConnected;
        private FontTextView mTvConnectedNum;
        private FontTextView mTvState;
        private SimpleDraweeView mSDVGrove01;
        private SimpleDraweeView mSDVGrove02;
        private SimpleDraweeView mSDVGrove03;
        private RelativeLayout mRlMoreGrove;
        private SimpleDraweeView mSDVGrove04;
        private FontTextView mTvMoreGroveNum;
        View mItemView;

        List<SimpleDraweeView> mGroveViews;

/*
        ImageView mBoardView;
        TextView mNameView;
        TextView mOnlineView;
        ImageView mOnlineLedView;
        TextView mXserverView;
        ImageView mFavoriteView;
        ImageView mPopMenuView;
        View mStatusView;

        TextView mRenameView;
        TextView mDetailView;
        TextView mRemoveView;

        List<ImageView> mGroveViews;
        TextView mGroveOverView;

        SwipeLayout mSwipeLayout;*/

        public MainViewHolder(View itemView, OnClickListener mOnClickListener) {
            super(itemView);
            mItemView = itemView;
            this.mOnClickListener = mOnClickListener;

            mSDVNode = (SimpleDraweeView) itemView.findViewById(R.id.mSDVNode);
            mTvTitle = (FontTextView) itemView.findViewById(R.id.mTvTitle);
            mTvConnected = (FontTextView) itemView.findViewById(R.id.mTvConnected);
            mTvConnectedNum = (FontTextView) itemView.findViewById(R.id.mTvConnectedNum);
            mTvState = (FontTextView) itemView.findViewById(R.id.mTvState);
            mSDVGrove01 = (SimpleDraweeView) itemView.findViewById(R.id.mSDVGrove01);
            mSDVGrove02 = (SimpleDraweeView) itemView.findViewById(R.id.mSDVGrove02);
            mSDVGrove03 = (SimpleDraweeView) itemView.findViewById(R.id.mSDVGrove03);
            mRlMoreGrove = (RelativeLayout) itemView.findViewById(R.id.mRlMoreGrove);
            mSDVGrove04 = (SimpleDraweeView) itemView.findViewById(R.id.mSDVGrove04);
            mTvMoreGroveNum = (FontTextView) itemView.findViewById(R.id.mTvMoreGroveNum);

            mGroveViews = new ArrayList<>();
            mGroveViews.add(0,mSDVGrove01);
            mGroveViews.add(1,mSDVGrove02);
            mGroveViews.add(2,mSDVGrove03);
            mGroveViews.add(3,mSDVGrove04);

            mItemView.setOnClickListener(this);
/*
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
            mRemoveView.setOnClickListener(this);*/
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v, getLayoutPosition());
            }

        }
    }
}
