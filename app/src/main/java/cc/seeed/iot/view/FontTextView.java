package cc.seeed.iot.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import cc.seeed.iot.R;


/**
 * Created by Administrator on 2016/3/1.
 */
public class FontTextView extends TextView {
    public FontTextView(Context context) {
        this(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CusFontTextView);
        String fontType = a.getString(R.styleable.CusFontTextView_cusTextFont);
        if (!TextUtils.isEmpty(fontType)) {
            try {
                Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontType);
                this.setTypeface(typeFace);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void setFont(int resoure) {
        String fontType = getResources().getString(resoure);
        if (!TextUtils.isEmpty(fontType)) {
            try {
                Typeface typeFace = Typeface.createFromAsset(this.getResources().getAssets(), "fonts/" + fontType + ".ttf");
                this.setTypeface(typeFace);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
    }
}
