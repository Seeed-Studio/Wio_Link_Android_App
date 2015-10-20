package cc.seeed.iot.util;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class DBHelper {
    public static List<Node> getNodesAll() {
        return new Select().from(Node.class).orderBy("node_sn").execute();
    }

    public static List<Node> getNodes(String node_sn) {
        return new Select().
                from(Node.class)
                .where("node_sn = ?", node_sn)
                .execute();
    }

    public static void delNodesAll() {
        new Delete()
                .from(Node.class)
                .execute();
    }

    public static List<GroverDriver> getGrovesAll() {
        return new Select().from(GroverDriver.class).orderBy("grove_id ASC").execute();
    }

    public static List<GroverDriver> getGroves(int id) {
        return new Select()
                .from(GroverDriver.class)
                .where("grove_id = ?", id)
                .execute();
    }

    public static List<GroverDriver> getGroves(String grove_name) {
        return new Select()
                .from(GroverDriver.class)
                .where("grove_name = ?", grove_name)
                .execute();
    }
}
