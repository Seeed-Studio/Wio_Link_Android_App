package cc.seeed.iot.entity;

import java.util.List;

/**
 * author: Jerry on 2016/7/4 16:38.
 * description:
 */
public class ServerBean {

    private long maxVerstamp;
    private long reqTime;

    private List<ContentBean> content;

    public long getReqTime() {
        return reqTime;
    }

    public void setReqTime(long reqTime) {
        this.reqTime = reqTime;
    }

    public long getMaxVerstamp() {
        return maxVerstamp;
    }

    public void setMaxVerstamp(long maxVerstamp) {
        this.maxVerstamp = maxVerstamp;
    }

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }


    public static class ContentBean {
        private String title;
        private String popText;
        private long popStartTime;
        private long popEndTime;
        private long serverEndTime;
        private List<String> boldText;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPopText() {
            return popText;
        }

        public void setPopText(String popText) {
            this.popText = popText;
        }

        public long getPopStartTime() {
            return popStartTime;
        }

        public void setPopStartTime(long popStartTime) {
            this.popStartTime = popStartTime;
        }

        public long getPopEndTime() {
            return popEndTime;
        }

        public void setPopEndTime(long popEndTime) {
            this.popEndTime = popEndTime;
        }

        public long getServerEndTime() {
            return serverEndTime;
        }

        public void setServerEndTime(long serverEndTime) {
            this.serverEndTime = serverEndTime;
        }

        public List<String> getBoldText() {
            return boldText;
        }

        public void setBoldText(List<String> boldText) {
            this.boldText = boldText;
        }
    }
}
