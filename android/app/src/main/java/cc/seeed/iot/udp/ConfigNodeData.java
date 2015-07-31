package cc.seeed.iot.udp;

/**
 * Created by tenwong on 15/7/30.
 */
public class ConfigNodeData {
    public String node_sn;
    public String ip;
    public String mac;

    public ConfigNodeData() {
        this.node_sn = "";
        this.ip = "";
        this.mac = "";
    }

    @Override
    public String toString() {
        return "node_sn: " + node_sn + " ip: " + ip + " mac: " + mac;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigNodeData))
            return false;
        ConfigNodeData other = (ConfigNodeData) o;
        if (!node_sn.equals(other.node_sn))
            return false;
        if (!ip.equals(other.ip))
            return false;
        if (!mac.equals(other.mac))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + mac.hashCode();
        return result;
    }
}
