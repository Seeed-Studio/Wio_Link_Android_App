package cc.seeed.iot.adapter.set_node;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.view.FontTextView;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroveFilterRecyclerAdapter extends RecyclerView.Adapter<GroveFilterRecyclerAdapter.MainViewHolder> {
    private List<String> grovesTypes;
    private MainViewHolder.MyItemClickListener mItemClickListener;

    private Context context;
    private SparseBooleanArray selectedItems;
    private int selectItemPosition = 0;

    public GroveFilterRecyclerAdapter(List<String> grovesTypes) {
        this.grovesTypes = grovesTypes;
        selectedItems = new SparseBooleanArray();
    }
    public GroveFilterRecyclerAdapter(String[] grovesTypes) {
        if (this.grovesTypes == null){
            this.grovesTypes = new ArrayList<>();
        }
        this.grovesTypes.clear();
        for (int i = 0; i< grovesTypes.length;i++){
            this.grovesTypes.add(grovesTypes[i]);
        }
      //  this.grovesTypes.a;
        selectedItems = new SparseBooleanArray();
    }

    public void setData(List<String> list){
        if (list != null ){
            grovesTypes.clear();
            grovesTypes.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grove_type_item, parent, false);
        MainViewHolder vh = new MainViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        String groveType = grovesTypes.get(position);
        final TextView mGroveTypeView = holder.mGroveTypeView;
        final View mView = holder.mView;
        mGroveTypeView.setText(groveType);
        if (selectedItems.get(position, false)) {
            mGroveTypeView.setTextColor(mView.getResources().getColor(R.color.tag_text_press));
            holder.mView.setSelected(true);
            holder.mViewTag.setVisibility(View.VISIBLE);
            selectItemPosition = position;
        } else {
            holder.mView.setSelected(false);
            holder.mViewTag.setVisibility(View.GONE);
            mGroveTypeView.setTextColor(mView.getResources().getColor(R.color.tag_text_normal));
        }

    }

    @Override
    public int getItemCount() {
        return grovesTypes.size();
    }

    public void setOnItemClickListener(MainViewHolder.MyItemClickListener listener) {
        this.mItemClickListener = listener;
    }


    public void updateSelection(int pos) {
        selectedItems.clear();
        selectedItems.put(pos, true);
        notifyDataSetChanged();
    }


    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        FontTextView mGroveTypeView;
        View mView;
        View mViewTag;

        public MainViewHolder(View itemView, MyItemClickListener listener) {
            super(itemView);
            mView = itemView;
            mGroveTypeView = (FontTextView) itemView.findViewById(R.id.grove_type);
            mViewTag =  itemView.findViewById(R.id.mViewTag);

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
