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
}
