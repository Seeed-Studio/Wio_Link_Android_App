package cc.seeed.iot.adapter.add_node;

import android.content.Context;
import android.net.wifi.ScanResult;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/25.
 */
public class WifiWioListRecyclerAdapter extends RecyclerView.Adapter<WifiRecyclerViewHolder> {
    private final static String TAG = "WifiListRecyclerAdapter";
    private List<ScanResult> wifiList;
    private Context context;
    WifiRecyclerViewHolder.IMyViewHolderClicks iMyViewHolderClicks;

    public WifiWioListRecyclerAdapter
            (List<ScanResult> wifiList,
             WifiRecyclerViewHolder.IMyViewHolderClicks iMyViewHolderClicks) {
        this.wifiList = wifiList;
        this.iMyViewHolderClicks = iMyViewHolderClicks;
    }

    @Override
    public WifiRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
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

    public ScanResult getItem(int position) {
        return wifiList.get(position);
    }

    public void updateAll(List<ScanResult> wifiList) {
        this.wifiList = wifiList;
        notifyDataSetChanged();
    }
}
