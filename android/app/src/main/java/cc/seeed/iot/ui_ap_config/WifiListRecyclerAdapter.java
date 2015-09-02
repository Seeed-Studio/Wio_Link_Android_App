package cc.seeed.iot.ui_ap_config;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.ui_smartconfig.SetNodeNameActivity;
import cc.seeed.iot.ui_smartconfig.SmartConnectActivity;

/**
 * Created by tenwong on 15/6/25.
 */
public class WifiListRecyclerAdapter extends RecyclerView.Adapter<WifiRecyclerViewHolder> {
    private final static String TAG = "WifiListRecyclerAdapter";
    private ArrayList<ScanResult> wifiList;
    private Context context;
    WifiRecyclerViewHolder.IMyViewHolderClicks iMyViewHolderClicks;

    public WifiListRecyclerAdapter(ArrayList<ScanResult> wifiList) {
        this.wifiList = wifiList;
    }

    @Override
    public WifiRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_list_item, parent, false);
        this.context = parent.getContext();

        WifiRecyclerViewHolder vh = new WifiRecyclerViewHolder(v, iMyViewHolderClicks);
        return vh;
    }

    @Override
    public void onBindViewHolder(WifiRecyclerViewHolder holder, final int position) {
        final ScanResult scanResult = wifiList.get(position);
        holder.mSsidView.setText(scanResult.SSID);
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    public void updateAll(ArrayList<ScanResult> wifiList) {
        this.wifiList = wifiList;
        notifyDataSetChanged();
    }
}
