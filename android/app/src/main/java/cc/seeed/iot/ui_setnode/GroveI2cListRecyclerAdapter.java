package cc.seeed.iot.ui_setnode;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
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
    private List<PinConfig> pinConfigs;

    private GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener mItemClickListener;

    public GroveI2cListRecyclerAdapter(List<PinConfig> pinConfigs) {
        this.pinConfigs = new ArrayList<>();
        this.pinConfigs = pinConfigs;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grove_i2c_list_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        PinConfig pinConfig = pinConfigs.get(position);
        ImageView grove_image = holder.grove_image;
        GroverDriver groverDriver = DBHelper.getGroves(pinConfig.grove_id).get(0);
        UrlImageViewHelper.setUrlDrawable(grove_image, groverDriver.ImageURL, R.drawable.grove_no,
                UrlImageViewHelper.CACHE_DURATION_INFINITE);
    }

    public void updateAll(List<PinConfig> pinConfigs) {
        this.pinConfigs = pinConfigs;
        notifyDataSetChanged();
    }

    public PinConfig getItem(int position){
        return pinConfigs.get(position);
    }

    @Override
    public int getItemCount() {
        return pinConfigs.size();
    }


    public static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView grove_image;
        View mView;

        public MainViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            grove_image = (ImageView) itemView.findViewById(R.id.grove_image);

//            mView.setOnClickListener(SetupIotNodeActivity.pin6OnClickListener);
            mView.setOnLongClickListener(SetupIotNodeActivity.pin6OnLongClickListener);
        }

    }

}
