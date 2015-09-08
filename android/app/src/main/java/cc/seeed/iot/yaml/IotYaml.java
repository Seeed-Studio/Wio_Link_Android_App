package cc.seeed.iot.yaml;

import android.util.Log;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tenwong on 15/8/7.
 */
public class IotYaml {
    static public String genYamlItem(int position, String groveInstanceName, String groveName) {
        String d = groveInstanceName + ":" + "\r\n";
        d = d + "  name: " + groveName + "\r\n";
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
                d = d + "    pintx: 3" + "\r\n";
                d = d + "    pinrx: 1" + "\r\n";
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

    static public Map<String, PinProperty> getNodeConfig(String yaml) {
        Map<String, PinProperty> nodeConfig = new HashMap<String, PinProperty>();
        try {
            YamlReader reader = new YamlReader(yaml);

            nodeConfig = reader.read(nodeConfig.getClass());
            Log.e("iot", "nodeConfig: " + nodeConfig);
        } catch (IOException e) {
            Log.e("iot", "error:" + e);
        }

        return nodeConfig;
    }
}
