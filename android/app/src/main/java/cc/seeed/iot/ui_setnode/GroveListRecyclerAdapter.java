package cc.seeed.iot.ui_setnode;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveListRecyclerAdapter extends RecyclerView.Adapter<GroveListRecyclerAdapter.MainViewHolder> {
    private ArrayList<GroverDriver> groves;
    private Context context;

    SparseBooleanArray selector;
    private GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener mItemClickListener;

    public GroveListRecyclerAdapter(ArrayList<GroverDriver> groves) {
        this.groves = groves;
        selector = new SparseBooleanArray();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grove_list_item, parent, false);

        v.setOnClickListener(SetupIotNodeActivity.mainOnClickListener);

        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        GroverDriver grove = groves.get(position);
        ImageView grove_image = holder.grove_image;
        UrlImageViewHelper.setUrlDrawable(grove_image, grove.ImageURL.toString());
        holder.mView.setPressed(selector.get(position, false));

    }

    public void selectItem(int position) {
        selector.clear();
        selector.put(position, true);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return groves.size();
    }


    public static class MainViewHolder extends RecyclerView.ViewHolder {
        ImageView grove_image;
        View mView;

        public MainViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            grove_image = (ImageView) itemView.findViewById(R.id.grove_image);

        }

    }

    public void setOnItemClickListener(GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener listener) {
        this.mItemClickListener = listener;
    }

}
