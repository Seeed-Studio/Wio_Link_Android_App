package cc.seeed.iot.ui_setnode.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by tenwong on 15/8/10.
 */
@Table(name = "pin_configs")
public class PinConfig extends Model {

    @Column()
    public String node_sn;

    @Column()
    public int position;

    @Column()
    public Boolean selected;

    @Column()
    public int grove_id;

    @Column(name = "grove_instance_name")
    public String groveInstanceName;

    @Override
    public String toString() {
        return "node_sn=" + node_sn + " position=" + position + " selected=" + selected +
                " grove_id=" + grove_id + " groveInstanceName=" + groveInstanceName;
    }
}
