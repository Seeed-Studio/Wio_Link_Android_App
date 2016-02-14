package cc.seeed.iot.webapi.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;
import java.util.Map;

/**
 * Created by tenwong on 15/6/25.
 */
@Table(name = "groves")
public class GroverDriver extends Model {
    @Column(name = "sku", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String SKU;

    @Column(name = "files")
    public List<String> Files;

    @Column(name = "class_file")
    public String ClassFile;

    @Column(name = "grove_name")
    public String GroveName;

    @Column(name = "image_url")
    public String ImageURL;

    @Column(name = "writes")
    public Map<String, WriteConfig> Writes;

    @Column(name = "has_power_off_func")
    public Boolean HasPowerOffFunc;

    @Column(name = "class_name")
    public String ClassName;

    @Column(name = "reads")
    public Map<String, ReadConfig> Reads;

    @Column(name = "can_get_last_error")
    public Boolean CanGetLastError;

    @Column(name = "events")
    public Map<String, String> Events;

    @Column(name = "has_event")
    public Boolean HasEvent;

    @Column(name = "interface_type")
    public String InterfaceType;

    @Column(name = "include_path")
    public String IncludePath;

    @Column(name = "has_power_on_func")
    public Boolean HasPowerOnFunc;

    @Column(name = "grove_id")
    public int ID;

    @Column(name = "construct_arg_list")
    public List<String> ConstructArgList;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GroverDriver)) return false;

        GroverDriver o = (GroverDriver) obj;
        return o.ID == this.ID;
    }

    @Override
    public int hashCode() {
        int result = 1;
        int PRIME = 17;

        result = result + PRIME + ID;

        return result;
    }

    @Override
    public String toString() {
        return "GroveName:" + GroveName + ",GroveID:" + ID + ",GroveSKU:" + SKU;
    }
}

