package cc.seeed.iot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lucky.indexablelistview.util.StringMatcher;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * author: Jerry on 2016/5/27 14:11.
 * description:
 */
public class GrovesAdapter extends BaseAdapter implements SectionIndexer {
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private List<GroverDriver> groves;
    private Context context;

    public GrovesAdapter(Context context, List<GroverDriver> groves) {
        this.groves = new ArrayList<>();
        this.groves = groves;
        this.context = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
           convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grove, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        onBindViewHolder(holder, position);

        return convertView;
    }

    public void onBindViewHolder(ViewHolder holder, final int position) {
        GroverDriver grove = groves.get(position);
       // UrlImageViewHelper.setUrlDrawable(grove_image, grove.ImageURL, R.mipmap.grove_default,UrlImageViewHelper.CACHE_DURATION_INFINITE);
//            String name = grove.GroveName.replaceFirst("Grove[\\s_-]+", "");
        ImgUtil.displayImg(holder.grove_image,grove.ImageURL,R.mipmap.grove_default);
        holder.mGroveNameView.setText(ToolUtil.getSimpleName(grove.GroveName));
        String interfaceType = grove.InterfaceType;
        if (interfaceType.equals("GPIO"))
            interfaceType = "Digital";
        holder.mGroveInterfaceView.setText(interfaceType);
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
        TextView mGroveNameView;
        TextView mGroveInterfaceView;
        View mView;

        public ViewHolder(View itemView) {
            mView = itemView;
            grove_image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            mGroveNameView = (TextView) itemView.findViewById(R.id.name);
            mGroveInterfaceView = (TextView) itemView.findViewById(R.id.interface_type);
        }
    }

    public void updateAll(List<GroverDriver> groverDrivers) {
        this.groves = groverDrivers;
        notifyDataSetChanged();
    }
}
