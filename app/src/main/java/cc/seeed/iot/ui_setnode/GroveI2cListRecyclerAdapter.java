package cc.seeed.iot.ui_setnode;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveI2cListRecyclerAdapter extends RecyclerView.Adapter<GroveI2cListRecyclerAdapter.MainViewHolder> {
    private static final String TAG = "...ListRecyclerAdapter";
    private List<PinConfig> pinConfigs;
    private OnLongClickListener mOnLongClickListener;
    private GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener mItemClickListener;

    public interface OnLongClickListener {
        void onLongClick(View v, int position);
    }

    public void setOnLongClickListen(OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    public GroveI2cListRecyclerAdapter(List<PinConfig> pinConfigs) {
        this.pinConfigs = new ArrayList<>();
        this.pinConfigs = pinConfigs;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grove_i2c_list_item, parent, false);
        return new MainViewHolder(v, mOnLongClickListener);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        PinConfig pinConfig = pinConfigs.get(position);
        ImageView grove_image = holder.grove_image;
        try {
            GroverDriver groverDriver = DBHelper.getGroves(pinConfig.sku).get(0);
            UrlImageViewHelper.setUrlDrawable(grove_image, groverDriver.ImageURL, R.drawable.grove_no,
                    UrlImageViewHelper.CACHE_DURATION_INFINITE);
        } catch (Exception e) {
            Log.e(TAG, "getGroves:" + e);
        }

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


    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private OnLongClickListener mOnLongClickListener;

        ImageView grove_image;
        View mView;

        public MainViewHolder(View itemView, OnLongClickListener mOnLongClickListener) {
            super(itemView);
            this.mOnLongClickListener = mOnLongClickListener;

            mView = itemView;
            grove_image = (ImageView) itemView.findViewById(R.id.grove_image);

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
