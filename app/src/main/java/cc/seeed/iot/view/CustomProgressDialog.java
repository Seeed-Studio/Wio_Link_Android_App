package cc.seeed.iot.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.InjectView;
import cc.seeed.iot.R;

/**
 * author: Jerry on 2016/5/31 14:51.
 * description:
 */
public class CustomProgressDialog extends ProgressDialog {
    ImageView mIv;
    TextView mTvMessage;

    public CustomProgressDialog(Context context) {
        this(context, 0);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        mIv = (ImageView) v.findViewById(R.id.mIv);
        mTvMessage = (TextView) v.findViewById(R.id.mTvMessage);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        mIv.setAnimation(animation);
        animation.start();

        setCanceledOnTouchOutside(false);
        setCancelable(false);
        this.show();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        this.setContentView(v,params);
    }

    @Override
    public void setMessage(CharSequence message) {
       mTvMessage.setText(message);
    }
}
