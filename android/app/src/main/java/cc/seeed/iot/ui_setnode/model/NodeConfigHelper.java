package cc.seeed.iot.ui_setnode.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;

/**
 * Created by tenwong on 15/8/10.
 */
public class NodeConfigHelper {
    private static final String TAG = "NodeConfigHelper";

    public static void saveToDB(NodeJson nodeJson, Node node) {
        List<PinConfig> pinConfigs = new ArrayList<>();
        List<Map<String, String>> connections = nodeJson.connections;
        try {
            for (Map<String, String> l : connections) {
                PinConfig pinConfig = new PinConfig();
                pinConfig.sku = l.get("sku");
                String port = l.get("port");
                String position = port.replaceAll(".*[^\\d](?=(\\d+))", "");
                pinConfig.position = Integer.parseInt(position);
                String abb = port.substring(0, port.length() - position.length());
                pinConfig.interfaceType = getInterfaceTypeFromAbb(abb);
                pinConfigs.add(pinConfig);
            }
        } catch (Exception e) {
            Log.e(TAG, "getNodeConfig:" + e);
        }

        PinConfigDBHelper.delPinConfig(node.node_sn);
        for (PinConfig pinConfig : pinConfigs) {
            pinConfig.node_sn = node.node_sn;
            pinConfig.save();
        }
    }

    public static NodeJson getConfigJson(List<PinConfig> pinConfigs, Node node) {
        NodeJson nodeJson = new NodeJson();
        nodeJson.board_name = node.board;
        List<Map<String, String>> connections = new ArrayList<>();
        for (PinConfig p : pinConfigs) {
            Map<String, String> map = new HashMap<>();
            map.put("port", getInterfaceTypeString(p.interfaceType) + p.position);
            map.put("sku", p.sku);
            connections.add(map);
        }
        nodeJson.connections = connections;
        return nodeJson;
    }

    private static String getInterfaceTypeFromAbb(String abb) {
        switch (abb){
            case "A":
                return InterfaceType.ANALOG;
            case "D":
                return InterfaceType.GPIO;
            case "I2C":
                return InterfaceType.I2C;
            case "UART":
                return InterfaceType.UART;
        }
        return "";
    }

    private static String getInterfaceTypeString(String interfaceType) {
        switch (interfaceType) {
            case InterfaceType.ANALOG:
                return "A";
            case InterfaceType.GPIO:
                return "D";
            case InterfaceType.I2C:
                return "I2C";
            case InterfaceType.UART:
                return "UART";
        }
        return "";
    }
}
