package cc.seeed.iot.ui_setnode.View;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.jauker.widget.BadgeView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.ui_setnode.model.InterfaceType;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/9/25.
 */
public class GrovePinsView {
    private static final String TAG = "GrovePinsView";
    private Context context;
    private View view;
    private Node node;
    public ImageButton[] pinViews;
    public BadgeView[] badgeViews;

    public GrovePinsView(Context context, View v, Node node) {
        this.context = context;
        this.view = v;
        this.node = node;
        if (node.board.equals(Constant.WIO_LINK_V1_0)) {
            this.pinViews = new ImageButton[6];
            this.badgeViews = new BadgeView[6];
            initLinkView();
        } else if (node.board.equals(Constant.WIO_NODE_V1_0)) {
            this.pinViews = new ImageButton[2];
            this.badgeViews = new BadgeView[2];
            initNodeView();
        }

    }

    private void initNodeView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.mNodeGrove_01);
        pinViews[1] = (ImageButton) view.findViewById(R.id.mNodeGrove_02);

        pinViews[0].setTag(new Tag(0, new String[]{InterfaceType.GPIO, InterfaceType.UART, InterfaceType.I2C}));
        pinViews[1].setTag(new Tag(1, new String[]{InterfaceType.GPIO, InterfaceType.ANALOG, InterfaceType.I2C}));

        badgeViews[0] = new BadgeView(context);
        badgeViews[1] = new BadgeView(context);

//        badgeViews[0].setTargetView(pinViews[0]);
//        badgeViews[1].setTargetView(pinViews[1]);
        setPinView(pinViews[0],badgeViews[0]);
        setPinView(pinViews[1],badgeViews[1]);

        badgeViews[0].setVisibility(View.GONE);
        badgeViews[1].setVisibility(View.GONE);

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                pinViews[pinConfig.position].setActivated(true);
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
            }
        }
    }

    private void initLinkView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.mLinkGrove_01);
        pinViews[1] = (ImageButton) view.findViewById(R.id.mLinkGrove_02);
        pinViews[2] = (ImageButton) view.findViewById(R.id.mLinkGrove_03);
        pinViews[3] = (ImageButton) view.findViewById(R.id.mLinkGrove_04);
        pinViews[4] = (ImageButton) view.findViewById(R.id.mLinkGrove_05);
        pinViews[5] = (ImageButton) view.findViewById(R.id.mLinkGrove_06);


        pinViews[0].setTag(new Tag(0, new String[]{InterfaceType.GPIO}));
        pinViews[1].setTag(new Tag(1, new String[]{InterfaceType.GPIO}));
        pinViews[2].setTag(new Tag(2, new String[]{InterfaceType.GPIO}));
        pinViews[3].setTag(new Tag(3, new String[]{InterfaceType.ANALOG}));
        pinViews[4].setTag(new Tag(4, new String[]{InterfaceType.UART}));
        pinViews[5].setTag(new Tag(5, new String[]{InterfaceType.I2C}));

        for (int i = 0; i < 6; i++) {
            badgeViews[i] = new BadgeView(context);
          //  badgeViews[i].setTargetView(pinViews[i]);
            badgeViews[i].setVisibility(View.GONE);
            setPinView(pinViews[i],badgeViews[i]);
        }

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                pinViews[pinConfig.position].setActivated(true);
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
            }
        }

    }

    public void updatePin(List<PinConfig> pinConfigs, int position) {
        for (PinConfig pinConfig : pinConfigs) {
            if (pinConfig.position == position) {
                try {
                    String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                    pinViews[pinConfig.position].setActivated(true);
                    UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,UrlImageViewHelper.CACHE_DURATION_INFINITE);
                } catch (Exception e) {
                    Log.e(TAG, "getGroves:" + e);
                }
            }
        }
    }

    public class Tag {
        public int position;
        public String[] interfaceTypes;

        public Tag(int position, String[] interfaceTypes) {
            this.position = position;
            this.interfaceTypes = interfaceTypes;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setPinView(ImageButton tragetView,BadgeView view){
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tragetView.measure(w, h);
        int height = tragetView.getMeasuredHeight();
        int width = tragetView.getMeasuredWidth();

      //  view.setBackgroundResource(R.drawable.grove_i2c_mark_bg);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height- ToolUtil.dp2px(23,context.getResources());
        params.width = width- ToolUtil.dp2px(20,context.getResources());
        view.setLayoutParams(params);
      // view.setBackgroundColor(Color.parseColor("#80000000"));
        view.setBackgroundResource(R.drawable.badgview_bg);
        //  view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.parseColor("#ffffff"));
        view.setTextSize(14);
        view.setBadgeGravity(Gravity.CENTER);
        view.setTargetView(tragetView);
    }


}