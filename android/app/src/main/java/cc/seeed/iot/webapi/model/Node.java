package cc.seeed.iot.webapi.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by tenwong on 15/6/25.
 */
@Table(name = "nodes")
public class Node extends Model {

    @Column()
    public String node_key;

    @Column()
    public String name;

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String node_sn;

    @Column()
    public Boolean online;

    @Column()
    public String dataxserver;

    @Column()
    public String board;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Node)) {
            return false;
        }

        Node obj = (Node) o;

        return this.node_sn.equals(obj.node_sn);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + node_sn.hashCode();
        return result;
    }
}
