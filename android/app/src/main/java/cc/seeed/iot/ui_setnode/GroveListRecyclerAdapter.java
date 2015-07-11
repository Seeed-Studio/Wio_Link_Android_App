package cc.seeed.iot.ui_setnode;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveListRecyclerAdapter extends RecyclerView.Adapter<GroveListRecyclerAdapter.MainViewHolder> {
    private ArrayList<GroverDriver> groves;
    private Context context;

    public GroveListRecyclerAdapter(ArrayList<GroverDriver> groves) {
        this.groves = groves;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grove_list_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        GroverDriver grove = groves.get(position);
        ImageView grove_image = holder.grove_image;
        UrlImageViewHelper.setUrlDrawable(grove_image, grove.ImageURL.toString());

//        Node node = nodes.get(position);
//        TextView tv_name = holder.tv_name;
//        ImageButton pop_menu = holder.pop_menu;
//        tv_name.setText(node.name);
//
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Snackbar.make(v,"Todo:set node", Snackbar.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(context,SetupIotNodeActivity.class);
//                intent.putExtra("position", position);
//                context.startActivity(intent);
//            }
//        });

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
}
