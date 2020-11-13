package cc.seeed.iot.adapter.set_node;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveI2cListRecyclerAdapter extends RecyclerView.Adapter<GroveI2cListRecyclerAdapter.MainViewHolder> {
    private static final String TAG = "...ListRecyclerAdapter";
    private List<PinConfig> pinConfigs;
    private OnLongClickListener mOnLongClickListener;
    private OnRemoveItemListener mOnRemoveItemListener;
    private GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener mItemClickListener;

    public interface OnLongClickListener {
        void onLongClick(View v, int position);
    }

    public void setOnLongClickListen(OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    public interface OnRemoveItemListener {
        void onRemoveItem(PinConfig pinConfig, int position,int totalPin);
    }

    public void setOnRemoveItemListener(OnRemoveItemListener l) {
        mOnRemoveItemListener = l;
    }

    public GroveI2cListRecyclerAdapter(List<PinConfig> pinConfigs) {
        this.pinConfigs = new ArrayList<>();
        this.pinConfigs = pinConfigs;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grove_i2c_list_item, parent, false);
        return new MainViewHolder(v, mOnLongClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        final PinConfig pinConfig = pinConfigs.get(position);
        try {
            GroverDriver groverDriver = DBHelper.getGroves(pinConfig.sku).get(0);
            ImgUtil.displayImg(holder.mIvGrove,groverDriver.ImageURL,R.mipmap.grove_default);
        } catch (Exception e) {
            Log.e(TAG, "getGroves:" + e);
        }

        List<GroverDriver> groves = DBHelper.getGroves(pinConfig.sku);
        if (groves != null && groves.size() > 0){
            holder.mTvName.setText(groves.get(0).GroveName);
        }else {
            holder.mTvName.setText("");
        }
        holder.mRlRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRemoveItemListener != null){
                    pinConfigs.remove(position);
                    mOnRemoveItemListener.onRemoveItem(pinConfig,position,pinConfigs.size());
                    notifyDataSetChanged();
                }
            }
        });

    }

    public void updateAll(List<PinConfig> pinConfigs) {
        this.pinConfigs = pinConfigs;
        notifyDataSetChanged();
    }

    public PinConfig getItem(int position) {
        return pinConfigs.get(position);
    }

    @Override
    public int getItemCount() {
        return pinConfigs.size();
    }


    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        private OnLongClickListener mOnLongClickListener;

        View mView;
        SimpleDraweeView mIvGrove;
        RelativeLayout mRlRemove;
        FontTextView mTvName;

        public MainViewHolder(View itemView, OnLongClickListener mOnLongClickListener) {
            super(itemView);
            this.mOnLongClickListener = mOnLongClickListener;

            mView = itemView;
            mIvGrove = (SimpleDraweeView) itemView.findViewById(R.id.mIvGrove);
            mRlRemove = (RelativeLayout) itemView.findViewById(R.id.mRlRemove);
            mTvName = (FontTextView) itemView.findViewById(R.id.mTvName);

            mView.setTag("I2cList");
            mView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnLongClickListener != null) {
                mOnLongClickListener.onLongClick(v, getLayoutPosition());
            }
            return true;
        }

    }
}
