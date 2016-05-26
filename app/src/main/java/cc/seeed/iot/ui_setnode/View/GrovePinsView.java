package cc.seeed.iot.ui_setnode.View;

import android.content.Context;
import android.util.Log;
import android.view.View;
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
        pinViews[0] = (ImageButton) view.findViewById(R.id.grove_0);
        pinViews[1] = (ImageButton) view.findViewById(R.id.grove_1);

        pinViews[0].setTag(new Tag(0, new String[]{InterfaceType.GPIO, InterfaceType.UART, InterfaceType.I2C}));
        pinViews[1].setTag(new Tag(1, new String[]{InterfaceType.GPIO, InterfaceType.ANALOG, InterfaceType.I2C}));

        badgeViews[0] = new BadgeView(context);
        badgeViews[1] = new BadgeView(context);

        badgeViews[0].setTargetView(pinViews[0]);
        badgeViews[1].setTargetView(pinViews[1]);

        badgeViews[0].setVisibility(View.GONE);
        badgeViews[1].setVisibility(View.GONE);

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
            }
        }
    }

    private void initLinkView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.grove_0);
        pinViews[1] = (ImageButton) view.findViewById(R.id.grove_1);
        pinViews[2] = (ImageButton) view.findViewById(R.id.grove_2);
        pinViews[3] = (ImageButton) view.findViewById(R.id.grove_3);
        pinViews[4] = (ImageButton) view.findViewById(R.id.grove_4);
        pinViews[5] = (ImageButton) view.findViewById(R.id.grove_5);


        pinViews[0].setTag(new Tag(0, new String[]{InterfaceType.GPIO}));
        pinViews[1].setTag(new Tag(1, new String[]{InterfaceType.GPIO}));
        pinViews[2].setTag(new Tag(2, new String[]{InterfaceType.GPIO}));
        pinViews[3].setTag(new Tag(3, new String[]{InterfaceType.ANALOG}));
        pinViews[4].setTag(new Tag(4, new String[]{InterfaceType.UART}));
        pinViews[5].setTag(new Tag(5, new String[]{InterfaceType.I2C}));

        for (int i = 0; i < 6; i++) {
            badgeViews[i] = new BadgeView(context);
            badgeViews[i].setTargetView(pinViews[i]);
            badgeViews[i].setVisibility(View.GONE);
        }

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
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
                    UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position], url, R.mipmap.grove_default,
                            UrlImageViewHelper.CACHE_DURATION_INFINITE);
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


}