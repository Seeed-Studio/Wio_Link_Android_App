package cc.seeed.iot.webapi.model;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by tenwong on 15/6/25.
 */
public class GroverDriver {
    public List<String> Files;
    public Map<String, List<String>> Inputs;
    public String ClassFile;
    public String GroveName;
    public Map<String, List<String>> Outputs;
    public URL ImageURL;
    public List<String> Events;
    public String ClassName;
    public Boolean CanGetLastError;
    public String InterfaceType;
    public Boolean HasEvent;
    public String IncludePath;
    public int ID;
    public List<String> ConstructArgList;
}
