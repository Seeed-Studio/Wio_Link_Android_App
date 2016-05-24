package cc.seeed.iot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import cc.seeed.iot.R;

/**
 * author: Jerry on 2016/5/17 17:44.
 * description:
 */
public class StepView extends RelativeLayout {
    private LinearLayout mLLLine;
    private View mLine1;
    private View mLine2;
    private ImageView mIvStep1;
    private FontTextView mTvStep1;
    private ImageView mIvStep2;
    private FontTextView mTvStep2;
    private ImageView mIvStep3;
    private FontTextView mTvStep3;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.step_view_layout, this);
        initView();
    }

    private void initView() {
        mLLLine = (LinearLayout) findViewById(R.id.mLLLine);
        mLine1 = findViewById(R.id.mLine1);
        mLine2 = findViewById(R.id.mLine2);
        mIvStep1 = (ImageView) findViewById(R.id.mIvStep1);
        mTvStep1 = (FontTextView) findViewById(R.id.mTvStep1);
        mIvStep2 = (ImageView) findViewById(R.id.mIvStep2);
        mTvStep2 = (FontTextView) findViewById(R.id.mTvStep2);
        mIvStep3 = (ImageView) findViewById(R.id.mIvStep3);
        mTvStep3 = (FontTextView) findViewById(R.id.mTvStep3);

    }

    public void setDoingStep(int step) {
        if (step < 1 || step > 3) {
            return;
        }
        switch (step) {
            case 1:
                mIvStep2.setBackgroundResource(R.drawable.circle_do1);
                mTvStep2.setTextColor(getResources().getColor(R.color.step_do1));
                mIvStep3.setBackgroundResource(R.drawable.circle_do2);
                mTvStep3.setTextColor(getResources().getColor(R.color.step_do2));
                break;
            case 2:
                mIvStep3.setBackgroundResource(R.drawable.circle_do1);
                mTvStep3.setTextColor(getResources().getColor(R.color.step_do1));
                mLine1.setBackgroundColor(getResources().getColor(R.color.step_did));
                break;
            case 3:
                mLine1.setBackgroundColor(getResources().getColor(R.color.step_did));
                mLine2.setBackgroundColor(getResources().getColor(R.color.step_did));
                break;
        }

    }
}
