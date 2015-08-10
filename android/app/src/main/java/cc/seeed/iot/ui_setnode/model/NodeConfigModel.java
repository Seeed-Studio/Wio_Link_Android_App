package cc.seeed.iot.ui_setnode.model;

import java.util.ArrayList;

import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.yaml.IotYaml;

/**
 * Created by tenwong on 15/8/10.
 */
public class NodeConfigModel {

    private ArrayList<PinConfig> pinConfigs;


    public NodeConfigModel() {
        pinConfigs = new ArrayList<PinConfig>(6);
        init();
    }

    private void init() {
        for (int i = 0; i < 6; i++) {
            PinConfig p = new PinConfig();

            p.position = i + 1;
            p.selected = false;
            p.groverDriver = null;
            pinConfigs.add(i, p);
        }
    }

    public ArrayList<PinConfig> getNodeConfig() {
        return pinConfigs;
    }

    public Boolean addPinNode(int position, GroverDriver groverDriver) {
        if (position > 6 || position < 1)
            return false;

        pinConfigs.get(position - 1).selected = true;
        pinConfigs.get(position - 1).groverDriver = groverDriver;

        return true;
    }

    public Boolean removePinNode(int position) {
        if (position > 6 || position < 1)
            return false;

        pinConfigs.get(position - 1).selected = false;
        pinConfigs.get(position - 1).groverDriver = null;
        return true;
    }

    public String getConfigYaml() {
        String y = "";
        for (PinConfig p : pinConfigs) {
            if (p.selected) {
                int position = p.position;
                String groveInstanceName = p.groverDriver.GroveName;
                String groveName = p.groverDriver.GroveName;
                y = y + IotYaml.genYamlItem(position, groveInstanceName, groveName);
            }
        }
        return y;
    }
}
