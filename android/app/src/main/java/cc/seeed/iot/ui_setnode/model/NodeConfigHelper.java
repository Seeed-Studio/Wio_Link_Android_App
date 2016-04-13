package cc.seeed.iot.ui_setnode.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;

/**
 * Created by tenwong on 15/8/10.
 */
public class NodeConfigHelper {
    private static final String TAG = "NodeConfigHelper";

    private Map<String, InterfacePin> linkMap = new HashMap<String, InterfacePin>() {{
        put("D0", new InterfacePin(InterfaceType.GPIO, 0));
        put("D1", new InterfacePin(InterfaceType.GPIO, 1));
        put("D2", new InterfacePin(InterfaceType.GPIO, 2));
        put("A0", new InterfacePin(InterfaceType.ANALOG, 3));
        put("UART0", new InterfacePin(InterfaceType.UART, 4));
        put("I2C0", new InterfacePin(InterfaceType.I2C, 5));
    }};

    private Map<String, InterfacePin> nodeMap = new HashMap<String, InterfacePin>() {{
        put("D0", new InterfacePin(InterfaceType.GPIO, 0));
        put("D1", new InterfacePin(InterfaceType.GPIO, 1));
        put("A0", new InterfacePin(InterfaceType.ANALOG, 1));
        put("UART0", new InterfacePin(InterfaceType.UART, 0));
        put("I2C0", new InterfacePin(InterfaceType.I2C, 0));
        put("I2C1", new InterfacePin(InterfaceType.I2C, 1));
    }};

    class InterfacePin {
        String interfaceType;
        int position;

        public InterfacePin(String type, int position) {
            this.interfaceType = type;
            this.position = position;
        }
    }

    public void saveToDB(NodeJson nodeJson, Node node) {
        List<PinConfig> pinConfigs = new ArrayList<>();
        List<Map<String, String>> connections = nodeJson.connections;
        try {
            for (Map<String, String> l : connections) {
                PinConfig pinConfig = new PinConfig();
                pinConfig.sku = l.get("sku");
                String port = l.get("port");
                if (node.board.equals(Constant.WIO_LINK_V1_0)) {
                    pinConfig.position = linkMap.get(port).position;
                    pinConfig.interfaceType = linkMap.get(port).interfaceType;
                } else if (node.board.equals(Constant.WIO_NODE_V1_0)) {
                    pinConfig.position = nodeMap.get(port).position;
                    pinConfig.interfaceType = nodeMap.get(port).interfaceType;
                }
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

    public NodeJson getConfigJson(List<PinConfig> pinConfigs, Node node) {
        NodeJson nodeJson = new NodeJson();
        nodeJson.board_name = node.board;
        List<Map<String, String>> connections = new ArrayList<>();
        for (PinConfig p : pinConfigs) {
            Map<String, String> map = new HashMap<>();
            if (node.board.equals(Constant.WIO_LINK_V1_0)) {
                map.put("port", getLinkPortName(p));
            } else if (node.board.equals(Constant.WIO_NODE_V1_0)) {
                map.put("port", getNodePortName(p));
            }
            map.put("sku", p.sku);
            connections.add(map);
        }
        nodeJson.connections = connections;
        return nodeJson;
    }

    private String getLinkPortName(PinConfig p) {
        switch (p.interfaceType) {
            case InterfaceType.GPIO:
                switch (p.position) {
                    case 0:
                        return "D0";
                    case 1:
                        return "D1";
                    case 2:
                        return "D2";
                }
            case InterfaceType.ANALOG:
                switch (p.position) {
                    case 3:
                        return "A0";
                }
            case InterfaceType.UART:
                switch (p.position) {
                    case 4:
                        return "UART0";
                }
            case InterfaceType.I2C:
                switch (p.position) {
                    case 5:
                        return "I2C0";
                }
        }
        return "";
    }

    private String getNodePortName(PinConfig p) {
        switch (p.interfaceType) {
            case InterfaceType.GPIO:
                switch (p.position) {
                    case 0:
                        return "D0";
                    case 1:
                        return "D1";
                }
            case InterfaceType.ANALOG:
                switch (p.position) {
                    case 1:
                        return "A0";
                }
            case InterfaceType.UART:
                switch (p.position) {
                    case 0:
                        return "UART0";
                }
            case InterfaceType.I2C:
                switch (p.position) {
                    case 0:
                        return "I2C0";
                    case 1:
                        return "I2C1";
                }
        }
        return "";
    }
}
