package cc.seeed.iot.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucky.indexablelistview.util.StringMatcher;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.activity.GroveDetailActivity;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * author: Jerry on 2016/5/27 14:11.
 * description:
 */
public class GrovesAdapter extends BaseAdapter implements SectionIndexer {
    private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
    private List<GroverDriver> groves;
    private Context context;
    private boolean isShowFullName = false;

    public GrovesAdapter(Context context, List<GroverDriver> groves) {
        this.groves = new ArrayList<>();
        this.groves = groves;
        this.context = context;
    }

    public GrovesAdapter(Context context, List<GroverDriver> groves,boolean isShowFullName) {
        this.groves = new ArrayList<>();
        this.groves = groves;
        this.context = context;
        this.isShowFullName = isShowFullName;
    }
    @Override
    public int getCount() {
        return groves.size();
    }

    @Override
    public Object getItem(int position) {
        return groves.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
           convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grove, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        onBindViewHolder(holder, position);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroverDriver grove = groves.get(position);
                Intent intent = new Intent(context, GroveDetailActivity.class);
                intent.putExtra(GroveDetailActivity.Intent_GroveSku,grove.SKU);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        GroverDriver grove = groves.get(position);
        ImgUtil.displayImg(holder.grove_image,grove.ImageURL,R.mipmap.grove_default);
        holder.mGroveNameView.setText(isShowFullName?grove.GroveName:ToolUtil.getSimpleName(grove.GroveName));
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = sectionIndex; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(ToolUtil.getSimpleName(((GroverDriver) getItem(j)).GroveName).charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(ToolUtil.getSimpleName(((GroverDriver) getItem(j)).GroveName).charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    public class ViewHolder  {
        SimpleDraweeView grove_image;
        FontTextView mGroveNameView;
        View mView;

        public ViewHolder(View itemView) {
            mView = itemView;
            grove_image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            mGroveNameView = (FontTextView) itemView.findViewById(R.id.name);
        }
    }

    public void updateAll(List<GroverDriver> groverDrivers) {
        this.groves = groverDrivers;
        notifyDataSetChanged();
    }
}
