package cc.seeed.iot.yaml;

import android.util.Log;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.seeed.iot.ui_setnode.model.PinConfig;

/**
 * Created by tenwong on 15/8/7.
 */
public class IotYaml {
    private static final String TAG = "IotYaml";

    static public String genYamlItem(int position, String groveInstanceName, String sku, String groveName) {
        String d = groveInstanceName + ":" + "\r\n";
        d = d + "  name: " + groveName + "\r\n";
        d = d + "  sku: " + sku + "\r\n";
        d = d + "  construct_arg_list:" + "\r\n";
        switch (position) {
            case 1: {
                d = d + "    pin: 14" + "\r\n";
            }
            break;
            case 2: {
                d = d + "    pin: 12" + "\r\n";
            }
            break;
            case 3: {
                d = d + "    pin: 13" + "\r\n";
            }
            break;
            case 4: {
                d = d + "    pin: 17" + "\r\n";
            }
            break;
            case 5: {
                d = d + "    pintx: 1" + "\r\n";
                d = d + "    pinrx: 3" + "\r\n";
            }
            break;
            case 6: {
                d = d + "    pinsda: 4" + "\r\n";
                d = d + "    pinscl: 5" + "\r\n";
            }
            break;
        }
        return d;
    }

    static public List<PinConfig> getNodeConfig(String yaml) {
        List<PinConfig> pinConfigs = new ArrayList<>();

        try {
            YamlReader reader = new YamlReader(yaml);
            Map nodeConfig = (Map) reader.read();

            Set set = nodeConfig.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                PinConfig pinConfig = new PinConfig();
                Map.Entry me = (Map.Entry) iterator.next();
                pinConfig.groveInstanceName = (String) me.getKey();
                pinConfig.sku = (String) ((Map) me.getValue()).get("sku");

                Map construct_arg_list = (Map) ((Map) me.getValue()).get("construct_arg_list");

                Set set_arg = construct_arg_list.keySet();
                if (set_arg.contains("pin")) {
                    String pin = (String) construct_arg_list.get("pin");
                    if (pin.equals("14")) {
                        pinConfig.position = 1;
                    } else if (pin.equals("12")) {
                        pinConfig.position = 2;
                    } else if (pin.equals("13")) {
                        pinConfig.position = 3;
                    } else if (pin.equals("17")) {
                        pinConfig.position = 4;
                    }
                } else if (set_arg.contains("pintx")) {
                    pinConfig.position = 5;
                } else if (set_arg.contains("pinsda")) {
                    pinConfig.position = 6;
                }
                pinConfig.selected = true;

                pinConfigs.add(pinConfig);
            }
        } catch (Exception e) {
            Log.e(TAG, "getNodeConfig:" + e);
        }

        return pinConfigs;
    }
}
