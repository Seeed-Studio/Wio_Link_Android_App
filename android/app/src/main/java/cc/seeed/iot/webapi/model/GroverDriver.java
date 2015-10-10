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
    public List<String> Files;

    public Map<String, List<String>> Inputs;

    @Column(name = "class_file")
    public String ClassFile;

    @Column(name = "grove_name")
    public String GroveName;

    public Map<String, List<String>> Outputs;

    @Column(name = "image_url")
    public String ImageURL;

    public List<String> Events;

    @Column(name = "class_name")
    public String ClassName;

    @Column(name = "can_get_last_error")
    public Boolean CanGetLastError;

    @Column(name = "interface_type")
    public String InterfaceType;

    @Column(name = "has_event")
    public Boolean HasEvent;

    @Column(name = "include_path")
    public String IncludePath;

    @Column(name = "grove_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int ID;

    public List<String> ConstructArgList;

    @Column(name = "image_url_path")
    public String ImageUrlPath;
}
