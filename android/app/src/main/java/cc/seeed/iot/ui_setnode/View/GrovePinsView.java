package cc.seeed.iot.ui_setnode.View;

import android.view.View;
import android.widget.ImageButton;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.ui_setnode.model.InterfaceType;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/9/25.
 */
public class GrovePinsView {
    private View view;
    private Node node;
    public ImageButton[] pinViews = new ImageButton[6];

    public GrovePinsView(View v, Node node) {
        view = v;
        this.node = node;
        initView();
    }

    private void initView() {
        pinViews[0] = (ImageButton) view.findViewById(R.id.grove_1);
        pinViews[1] = (ImageButton) view.findViewById(R.id.grove_2);
        pinViews[2] = (ImageButton) view.findViewById(R.id.grove_3);
        pinViews[3] = (ImageButton) view.findViewById(R.id.grove_4);
        pinViews[4] = (ImageButton) view.findViewById(R.id.grove_5);
        pinViews[5] = (ImageButton) view.findViewById(R.id.grove_6);

        pinViews[0].setTag(new Tag(1, InterfaceType.GPIO));
        pinViews[1].setTag(new Tag(2, InterfaceType.GPIO));
        pinViews[2].setTag(new Tag(3, InterfaceType.GPIO));
        pinViews[3].setTag(new Tag(4, InterfaceType.ANALOG));
        pinViews[4].setTag(new Tag(5, InterfaceType.UART));
        pinViews[5].setTag(new Tag(6, InterfaceType.I2C));


        List<PinConfig> pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            String url = DBHelper.getGroves(pinConfig.grove_id).get(0).ImageURL;
            UrlImageViewHelper.setUrlDrawable(pinViews[pinConfig.position-1], url, R.drawable.grove_cold,
                    UrlImageViewHelper.CACHE_DURATION_INFINITE);
        }

    }

//    @Override
//    public boolean onDrag(View v, DragEvent event) {
//        int action = event.getAction();
//        switch (action) {
//            case DragEvent.ACTION_DRAG_STARTED:
//                break;
//            case DragEvent.ACTION_DRAG_ENTERED:
//                break;
//            case DragEvent.ACTION_DRAG_LOCATION:
//                break;
//            case DragEvent.ACTION_DRAG_EXITED:
//                break;
//            case DragEvent.ACTION_DRAG_ENDED:
//                break;
//        }
//        return false;
//    }

    public void activatedPin(int pin) {
        pinViews[pin - 1].setActivated(true);
    }

    public void removeActivatedPin(int pin) {
        pinViews[pin - 1].setActivated(false);
    }

    public void selectedPin(int pin) {
        pinViews[pin - 1].setSelected(true);
    }

    public void removeSelectedPin(int pin) {
        pinViews[pin - 1].setSelected(false);
    }

    public void setImage(int pin, int resId) {
        pinViews[pin - 1].setImageResource(resId);
    }

    public void RemoveImage(int pin) {
        pinViews[pin - 1].setImageResource(0);
    }

    public class Tag {
        public int position;
        public String interfaceType;

        public Tag(int position, String interfaceType) {
            this.position = position;
            this.interfaceType = interfaceType;
        }
    }


}