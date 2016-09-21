package cc.seeed.iot.entity;

/**
 * Created by seeed on 2016/3/4.
 */
public class UpdateApkBean {
    public String version_name;
    public String version_message;
    public String add_time;
    public boolean is_force;
    public String version_title;
    public String version_type;
    public String url;

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getVersion_message() {
        return version_message;
    }

    public void setVersion_message(String version_message) {
        this.version_message = version_message;
    }

    public String getVersion_title() {
        return version_title;
    }

    public void setVersion_title(String version_title) {
        this.version_title = version_title;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public boolean is_force() {
        return is_force;
    }

    public void setIs_force(boolean is_force) {
        this.is_force = is_force;
    }

    public String getVersion_type() {
        return version_type;
    }

    public void setVersion_type(String version_type) {
        this.version_type = version_type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UpdateApkBean(String version_name, String version_message, String add_time, boolean is_force, String version_type, String url) {
        this.version_name = version_name;
        this.version_message = version_message;
        this.add_time = add_time;
        this.is_force = is_force;
        this.version_type = version_type;
        this.url = url;
    }
}
