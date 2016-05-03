package cc.seeed.iot.util;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class DBHelper {
    public static List<Node> getNodesAll() {
        return new Select()
                .from(Node.class)
                .execute();
    }

    public static List<Node> getNodes(String node_sn) {
        return new Select().
                from(Node.class)
                .where("node_sn = ?", node_sn)
                .execute();
    }

    public static void delNode(Node node) {
        delNode(node.node_sn);
    }

    public static void delNode(String node_sn) {
        new Delete().
                from(Node.class)
                .where("node_sn = ?", node_sn)
                .execute();
    }

    public static void delNodesAll() {
        new Delete()
                .from(Node.class)
                .execute();
    }

    public static List<Node> saveNodes(List<Node> nodes) {
        for (Node node : getNodesAll()) {
            if (!nodes.contains(node)) {
                delNode(node);
            }
        }

        for (Node node : nodes) {
            node.save();
        }
        return nodes;
    }

    public static Node saveNode(Node node) {
        node.save();
        return node;
    }


    public static List<GroverDriver> getGrovesAll() {
        return new Select().from(GroverDriver.class).orderBy("grove_name ASC").execute();
    }


    public static List<GroverDriver> getGroves(String sku) {
        return new Select()
                .from(GroverDriver.class)
                .where("sku = ?", sku)
                .execute();
    }

    public static void delGrovesAll(){
        new Delete()
                .from(GroverDriver.class)
                .execute();
    }


}
