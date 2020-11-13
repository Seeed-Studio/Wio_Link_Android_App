package cc.seeed.iot.ui_main;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/6/25.
 */
public class NodeListRecyclerAdapter extends RecyclerSwipeAdapter<NodeListRecyclerAdapter.MainViewHolder> {
    private static final String TAG = "NodeListRecyclerAdapter";
    private List<Node> nodes = new ArrayList<>();
    private Context context;

    private OnClickListener mOnClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public interface OnClickListener {
        void onClick(View v, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int position);
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        mOnItemLongClickListener = l;
    }

    public NodeListRecyclerAdapter(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node1, parent, false);
        return new MainViewHolder(v, mOnClickListener, mOnItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        if (position == 0) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mLlItem.getLayoutParams();
            int top = ToolUtil.dp2px(13, context.getResources());
            int left = ToolUtil.dp2px(12, context.getResources());
            int right = ToolUtil.dp2px(12, context.getResources());
            int buttom = ToolUtil.dp2px(13, context.getResources());
            params.setMargins(left, top, right, buttom);
            holder.mLlItem.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mLlItem.getLayoutParams();
            int top = ToolUtil.dp2px(0, context.getResources());
            int left = ToolUtil.dp2px(12, context.getResources());
            int right = ToolUtil.dp2px(12, context.getResources());
            int buttom = ToolUtil.dp2px(13, context.getResources());
            params.setMargins(left, top, right, buttom);
            holder.mLlItem.setLayoutParams(params);
        }

        Node node = nodes.get(position);

        if (node.board == null) {
            node.board = Constant.WIO_LINK_V1_0;
        }
        switch (node.board) {
            default:
            case Constant.WIO_LINK_V1_0:
                if (node.online) {
                    holder.mSDVNode.setImageResource(R.mipmap.link_small_01);
                }else {
                    holder.mSDVNode.setImageResource(R.mipmap.link_small_offline);
                }
                break;
            case Constant.WIO_NODE_V1_0:
                if (node.online) {
                    holder.mSDVNode.setImageResource(R.mipmap.node_small_01);
                }else {
                    holder.mSDVNode.setImageResource(R.mipmap.node_small_offline);
                }
                break;
        }
        holder.mTvTitle.setText(TextUtils.isEmpty(node.name) ? "Wio Link" + position + 1 : node.name);
        if (node.online) {
            holder.mTvState.setText(R.string.online);
            holder.mTvState.setBackgroundResource(R.drawable.online_state_bg);
        } else {
            holder.mTvState.setText(R.string.offline);
            holder.mTvState.setBackgroundResource(R.drawable.offline_state_bg);
        }

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (int i = 0; i < 4; i++) {
            try {
                holder.mGroveViews.get(i).setVisibility(View.VISIBLE);
                PinConfig pinConfig = pinConfigs.get(i); //IndexOutOfBoundsException
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL; //maybe null
                ImgUtil.displayImg(holder.mGroveViews.get(i), url, R.mipmap.grove_default);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
                holder.mGroveViews.get(i).setVisibility(View.GONE);
            }
        }

        holder.mTvConnectedNum.setText(String.format("%s", String.valueOf(pinConfigs.size())));
        if (pinConfigs.size() > 4) {
            Integer over_num = pinConfigs.size() - 4;
            holder.mTvMoreGroveNum.setVisibility(View.VISIBLE);
            holder.mTvMoreGroveNum.setText(String.format("+%s", over_num.toString()));
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
        if (position == 0) {
            notifyItemChanged(position);
        }
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

    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private OnClickListener mOnClickListener;
        private OnItemLongClickListener mOnItemLongClickListener;

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
        private LinearLayout mLlItem;
        View mItemView;

        List<SimpleDraweeView> mGroveViews;

        public MainViewHolder(View itemView, OnClickListener mOnClickListener, OnItemLongClickListener onItemLongClickListener) {
            super(itemView);
            mItemView = itemView;
            this.mOnClickListener = mOnClickListener;
            this.mOnItemLongClickListener = onItemLongClickListener;

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
            mLlItem = (LinearLayout) itemView.findViewById(R.id.mLlItem);

            mGroveViews = new ArrayList<>();
            mGroveViews.add(0, mSDVGrove01);
            mGroveViews.add(1, mSDVGrove02);
            mGroveViews.add(2, mSDVGrove03);
            mGroveViews.add(3, mSDVGrove04);

            mItemView.setOnClickListener(this);
            mItemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v, getLayoutPosition());
            }

        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, getLayoutPosition());
            }
            return false;
        }
    }
}
