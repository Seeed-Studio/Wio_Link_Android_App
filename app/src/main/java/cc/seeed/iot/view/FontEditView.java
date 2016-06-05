package cc.seeed.iot.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import cc.seeed.iot.R;


/**
 * Created by Administrator on 2016/3/1.
 */
public class FontEditView extends EditText {
    public FontEditView(Context context) {
        this(context, null);
    }

    public FontEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CusFontEditView);
        String fontType = a.getString(R.styleable.CusFontEditView_cusEditFont);
        if (!TextUtils.isEmpty(fontType)){
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontType);
            this.setTypeface(typeFace);
        }
    }

    public void setFont(int resoure) {
        String fontType = getResources().getString(resoure);
        Typeface typeFace = Typeface.createFromAsset(this.getResources().getAssets(), "fonts/" + fontType + ".ttf");
        this.setTypeface(typeFace);
    }
}
