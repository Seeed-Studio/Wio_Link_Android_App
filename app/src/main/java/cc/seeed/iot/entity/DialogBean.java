package cc.seeed.iot.entity;

/**
 * author: Jerry on 2016/5/20 17:37.
 * description:
 */
public class DialogBean {
    public  String title;
    public String okName;
    public String cancelName;
    public String content;

    public DialogBean() {
    }

    public DialogBean(String title, String okName, String cancelName, String content) {
        this.title = title;
        this.okName = okName;
        this.cancelName = cancelName;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOkName() {
        return okName;
    }

    public void setOkName(String okName) {
        this.okName = okName;
    }

    public String getCancelName() {
        return cancelName;
    }

    public void setCancelName(String cancelName) {
        this.cancelName = cancelName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
