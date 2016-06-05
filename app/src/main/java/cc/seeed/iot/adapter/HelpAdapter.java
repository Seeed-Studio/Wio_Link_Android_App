package cc.seeed.iot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.entity.FAQBean;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;
import pl.droidsonroids.gif.GifImageView;

/**
 * author: Jerry on 2016/5/27 14:11.
 * description:
 */
public class HelpAdapter extends BaseAdapter {
    private List<FAQBean> beans;
    private Context context;

    public HelpAdapter(Context context, List<FAQBean> beans) {
        this.beans = new ArrayList<>();
        this.beans = beans;
        this.context = context;
    }

    @Override
    public int getCount() {
        return beans.size();
    }

    @Override
    public Object getItem(int position) {
        return beans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        onBindViewHolder(holder, position);
        return convertView;
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        FAQBean bean = beans.get(position);
        holder.mIvState.setImageResource(bean.url);
        holder.mTvQues.setText(bean.ques);
        holder.mTvAnswer.setText(bean.answer);
    }

    public void updateAll(List<FAQBean> beans) {
        this.beans = beans;
        notifyDataSetChanged();
    }

    public class ViewHolder {
        public final GifImageView mIvState;
        public final FontTextView mTvQues;
        public final FontTextView mTvAnswer;
        public final View root;

        public ViewHolder(View root) {
            mIvState = (GifImageView) root.findViewById(R.id.mIvState);
            mTvQues = (FontTextView) root.findViewById(R.id.mTvQues);
            mTvAnswer = (FontTextView) root.findViewById(R.id.mTvAnswer);
            this.root = root;
        }
    }
}
