package cc.seeed.iot.ui_setnode;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveFilterRecyclerAdapter extends RecyclerView.Adapter<GroveFilterRecyclerAdapter.MainViewHolder> {
    private String[] grovesTypes;
    private MainViewHolder.MyItemClickListener mItemClickListener;

    private static Context context;


    public GroveFilterRecyclerAdapter(String[] grovesTypes) {
        this.grovesTypes = grovesTypes;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grove_type_item, parent, false);
        MainViewHolder vh = new MainViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        String groveType = grovesTypes[position];
        final TextView mGroveTypeView = holder.mGroveTypeView;
        final View mView = holder.mView;
        mGroveTypeView.setText(groveType);

//        mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Snackbar.make(v, "Todo:set node", Snackbar.LENGTH_SHORT).show();
//                mGroveTypeView.setTextColor(context.getResources().getColor(R.color.window_background_dark));
//
////                onSetGrvoesFilter(String groveType);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return grovesTypes.length;
    }

    public void setOnItemClickListener(MainViewHolder.MyItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        TextView mGroveTypeView;
        View mView;

        public MainViewHolder(View itemView, MyItemClickListener listener) {
            super(itemView);
            mView = itemView;
            mGroveTypeView = (TextView) itemView.findViewById(R.id.grove_type);

            this.mListener = listener;
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(context, "Item click nr: " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition());
            }
        }

        public interface MyItemClickListener {
            public void onItemClick(View view, int postion);
        }
    }


}
