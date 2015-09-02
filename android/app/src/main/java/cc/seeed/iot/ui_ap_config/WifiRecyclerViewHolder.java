package cc.seeed.iot.ui_ap_config;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/25.
 */
public class WifiRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView mSsidView;
    public IMyViewHolderClicks mListener;

    public WifiRecyclerViewHolder(View itemView, IMyViewHolderClicks listener) {
        super(itemView);
        mListener = listener;
        mSsidView = (TextView) itemView.findViewById(R.id.wifi_ssid);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mListener.onItem(v);
    }

    public interface IMyViewHolderClicks {
        public void onItem(View caller);
    }
}
