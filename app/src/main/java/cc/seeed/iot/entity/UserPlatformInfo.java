package cc.seeed.iot.entity;

import java.io.Serializable;

/**
 * author: Jerry on 2016/7/13 17:51.
 * description:
 */
public class UserPlatformInfo implements Serializable{
    String platformNickname;
    int platformType;//第三方类型；1微博，2facebook，3github，4Twitter，5googleplus
    String platformID;
    String platformAvatar;
    String platformEmail;

    public String getPlatformNickname() {
        return platformNickname;
    }

    public void setPlatformNickname(String platformNickname) {
        this.platformNickname = platformNickname;
    }

    public int getPlatformType() {
        return platformType;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }

    public String getPlatformID() {
        return platformID;
    }

    public void setPlatformID(String platformID) {
        this.platformID = platformID;
    }

    public String getPlatformAvatar() {
        return platformAvatar;
    }

    public void setPlatformAvatar(String platformAvatar) {
        this.platformAvatar = platformAvatar;
    }

    public String getPlatformEmail() {
        return platformEmail;
    }

    public void setPlatformEmail(String platformEmail) {
        this.platformEmail = platformEmail;
    }
}
