package cc.seeed.iot.entity;

/**
 * Created by seeed on 2016/3/22.
 */
public class User {
    public String userid;
    public String token;
    public String password;
    public String email;
    public int admin;
    public String phone;
    public String nickname;
    public String avater;
    public String source;
    public String facebook_id;
    public String webchat_id;
    public String webchat_nickname;
    public String webchat_avatar;
    public String facebook_nickname;
    public String facebook_avatar;

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public String getAvater() {
        return avater;
    }

    public void setAvater(String avater) {
        this.avater = avater;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avater;
    }

    public void setAvatar(String avatar) {
        this.avater = avatar;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }

    public String getWebchat_id() {
        return webchat_id;
    }

    public void setWebchat_id(String webchat_id) {
        this.webchat_id = webchat_id;
    }

    public String getWebchat_nickname() {
        return webchat_nickname;
    }

    public void setWebchat_nickname(String webchat_nickname) {
        this.webchat_nickname = webchat_nickname;
    }

    public String getWebchat_avatar() {
        return webchat_avatar;
    }

    public void setWebchat_avatar(String webchat_avatar) {
        this.webchat_avatar = webchat_avatar;
    }

    public String getFacebook_nickname() {
        return facebook_nickname;
    }

    public void setFacebook_nickname(String facebook_nickname) {
        this.facebook_nickname = facebook_nickname;
    }

    public String getFacebook_avatar() {
        return facebook_avatar;
    }

    public void setFacebook_avatar(String facebook_avatar) {
        this.facebook_avatar = facebook_avatar;
    }

    public User() {
    }

    public User(String userid, String token, String password, String email, int admin, String phone, String nickname, String avater, String source, String facebook_id, String webchat_id, String webchat_nickname, String webchat_avatar, String facebook_nickname, String facebook_avatar) {
        this.userid = userid;
        this.token = token;
        this.password = password;
        this.email = email;
        this.admin = admin;
        this.phone = phone;
        this.nickname = nickname;
        this.avater = avater;
        this.source = source;
        this.facebook_id = facebook_id;
        this.webchat_id = webchat_id;
        this.webchat_nickname = webchat_nickname;
        this.webchat_avatar = webchat_avatar;
        this.facebook_nickname = facebook_nickname;
        this.facebook_avatar = facebook_avatar;
    }
}
