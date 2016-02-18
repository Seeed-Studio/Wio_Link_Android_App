package cc.seeed.iot.ui_setnode.View;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
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
    private View view;
    private Node node;
    public ImageButton[] pinViews;

    public GrovePinsView(View v, Node node) {
        this.view = v;
        this.node = node;
        if (node.board.equals(Constant.WIOLINK_V10)) {
            this.pinViews = new ImageButton[6];
            initLinkView();
        } else if (node.board.equals(Constant.WIONODE_V10)) {
            this.pinViews = new ImageButton[2];
            initNodeView();
        } else {

        }

    }

    private void initNodeView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.grove_0);
        pinViews[1] = (ImageButton) view.findViewById(R.id.grove_1);

        pinViews[0].setTag(new Tag(1, new String[]{InterfaceType.GPIO, InterfaceType.UART, InterfaceType.I2C}));
        pinViews[1].setTag(new Tag(2, new String[]{InterfaceType.GPIO, InterfaceType.ANALOG, InterfaceType.I2C}));

        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position - 1], url, R.drawable.grove_no,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
            }
        }
    }

    private void initLinkView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.grove_1);
        pinViews[1] = (ImageButton) view.findViewById(R.id.grove_2);
        pinViews[2] = (ImageButton) view.findViewById(R.id.grove_3);
        pinViews[3] = (ImageButton) view.findViewById(R.id.grove_4);
        pinViews[4] = (ImageButton) view.findViewById(R.id.grove_5);
        pinViews[5] = (ImageButton) view.findViewById(R.id.grove_6);


        pinViews[0].setTag(new Tag(1, new String[]{InterfaceType.GPIO}));
        pinViews[1].setTag(new Tag(2, new String[]{InterfaceType.GPIO}));
        pinViews[2].setTag(new Tag(3, new String[]{InterfaceType.GPIO}));
        pinViews[3].setTag(new Tag(4, new String[]{InterfaceType.ANALOG}));
        pinViews[4].setTag(new Tag(5, new String[]{InterfaceType.UART}));
        pinViews[5].setTag(new Tag(6, new String[]{InterfaceType.I2C}));


        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            try {
                String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position - 1], url, R.drawable.grove_no,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
            } catch (Exception e) {
                Log.e(TAG, "getGroves:" + e);
            }
        }

    }

    public void updatePin6(List<PinConfig> pinConfigs) {
        for (PinConfig pinConfig : pinConfigs) {
            if (pinConfig.position == 6) {
                try {
                    String url = DBHelper.getGroves(pinConfig.sku).get(0).ImageURL;
                    UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position - 1], url, R.drawable.grove_no,
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