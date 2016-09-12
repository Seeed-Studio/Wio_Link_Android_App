package cc.seeed.iot.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.seeed.iot.App;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.EncryptUtil;


/**
 * Created by seeed on 2016/2/27.
 */
public class BaseLogic implements CmdConst {
    protected App app = App.getApp();

    public static String getSign(String url, String time) {
        String sign = "";
        List<String> list = new ArrayList<>();
        list.add(Common.WioLink_AppId);
        list.add(Common.WioLink_AppKey);
        list.add(Common.WioLink_Common);
        list.add(url);
        list.add(time);

        Collections.sort(list);

        for (String str : list) {
            sign += str;
        }

        sign = EncryptUtil.SHA1(sign);
        return sign;
    }

    public static String getToken() {
        User userBean = UserLogic.getInstance().getUser();
        if (userBean != null) {
            String decrypt = EncryptUtil.decrypt(userBean.getToken(), Common.API_GET_TOKEN_KEY);
            String encrypt = EncryptUtil.encrypt(decrypt, Common.API_CHECK_TOKEN_KEY);
            return encrypt;
        }
        return null;
    }


}
