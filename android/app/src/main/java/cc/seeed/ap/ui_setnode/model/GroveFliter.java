package cc.seeed.ap.ui_setnode.model;

import java.util.ArrayList;

import cc.seeed.ap.webapi.model.GroverDriver;

/**
 * Created by tenwong on 15/8/10.
 */
public class GroveFliter {
    public static final String I2C = "I2C";
    public static final String GPIO = "GPIO";
    public static final String UART = "UART";
    public static final String ANALOG = "ANALOG";

    public ArrayList<GroverDriver> groverDrivers;

    public GroveFliter(ArrayList<GroverDriver> groverDrivers) {
        this.groverDrivers = groverDrivers;
    }

    public ArrayList<GroverDriver> getGroveFilterInterface(String pinInterface) {
        ArrayList<GroverDriver> groveFilters = new ArrayList<GroverDriver>();

        if (pinInterface.equals(I2C)) {
            for (GroverDriver g : groverDrivers) {
                if (g.InterfaceType.equals(I2C))
                    groveFilters.add(g);
            }
        } else if (pinInterface.equals(GPIO)) {
            for (GroverDriver g : groverDrivers) {
                if (g.InterfaceType.equals(GPIO))
                    groveFilters.add(g);
            }
        } else if (pinInterface.equals(UART)) {
            for (GroverDriver g : groverDrivers) {
                if (g.InterfaceType.equals(UART))
                    groveFilters.add(g);
            }
        } else if (pinInterface.equals(ANALOG)) {
            for (GroverDriver g : groverDrivers) {
                if (g.InterfaceType.equals(ANALOG))
                    groveFilters.add(g);
            }
        } else {
        }

        return groveFilters;
    }
}
