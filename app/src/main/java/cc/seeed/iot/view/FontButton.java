package cc.seeed.iot.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import cc.seeed.iot.R;


/**
 * Created by Administrator on 2016/3/1.
 */
public class FontButton extends Button {
    public FontButton(Context context) {
        this(context, null);
    }

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CusFontButton);
        String fontType = a.getString(R.styleable.CusFontButton_cusButtonFont);
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
