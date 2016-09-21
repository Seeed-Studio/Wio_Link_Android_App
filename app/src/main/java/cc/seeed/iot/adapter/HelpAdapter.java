package cc.seeed.iot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.entity.FAQBean;
import cc.seeed.iot.view.FontTextView;
import pl.droidsonroids.gif.GifImageView;

/**
 * author: Jerry on 2016/5/27 14:11.
 * description:
 */
public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder> {
    private List<FAQBean> beans;
    private Context context;

    public HelpAdapter(List<FAQBean> beans) {
        this.beans = new ArrayList<>();
        this.beans = beans;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        FAQBean bean = beans.get(position);
        holder.mIvState.setImageResource(bean.url);
        holder.mTvQues.setText(bean.ques);
        holder.mTvAnswer.setText(bean.answer);
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    public FAQBean getItem(int position) {
        return beans.get(position);
    }

    public void updateAll(List<FAQBean> beans) {
        this.beans = beans;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public GifImageView mIvState;
        public FontTextView mTvQues;
        public FontTextView mTvAnswer;
        public View root;

        public ViewHolder(View root) {
            super(root);
            mIvState = (GifImageView) root.findViewById(R.id.mIvState);
            mTvQues = (FontTextView) root.findViewById(R.id.mTvQues);
            mTvAnswer = (FontTextView) root.findViewById(R.id.mTvAnswer);
            this.root = root;
        }
    }
}
