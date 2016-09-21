package cc.seeed.iot.adapter.add_node;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.seeed.iot.R;
import cc.seeed.iot.view.FontTextView;


/**
 * Created by tenwong on 15/6/25.
 */
public class WifiRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    FontTextView mSsidView;
    public IMyViewHolderClicks mListener;
    public View mItemView;
    public LinearLayout mLlItem;

    public WifiRecyclerViewHolder(View itemView, IMyViewHolderClicks listener) {
        super(itemView);
        this.mItemView = itemView;
        mListener = listener;
        mSsidView = (FontTextView) itemView.findViewById(R.id.wifi_ssid);
        mLlItem = (LinearLayout) itemView.findViewById(R.id.mLlItem);
        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        mListener.onItem(v);
    }

    public interface IMyViewHolderClicks {
        void onItem(View caller);
    }
}
