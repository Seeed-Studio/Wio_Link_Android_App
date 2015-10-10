package cc.seeed.iot.util;

import com.activeandroid.query.Select;

import java.util.List;

import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class DBHelper {
    public static List<Node> getNodesAll() {
        return new Select().from(Node.class).orderBy("node_sn").execute();
    }

    public static List<GroverDriver> getGrovesAll() {
        return new Select().from(GroverDriver.class).orderBy("grove_id ASC").execute();
    }
//
//    /**
//     * 通过某个字段进行搜索
//     *
//     * @param required 条件
//     * @return 查到的笔记
//     */
//    public static List<NoteInfo> search(String required) {
//        return new Select().from(NoteInfo.class).where("content = ?", required).execute();
//    }
//
//    /**
//     * 删除笔记
//     *
//     * @param info 笔记信息
//     */
//    public static void delete(NoteInfo info) {
//        info.delete(NoteInfo.class, 1);
//    }
}
