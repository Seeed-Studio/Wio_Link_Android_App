package cc.seeed.iot;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tenwong on 15/7/9.
 */
public class MyApplication extends com.activeandroid.app.Application {
    private SharedPreferences sp;

    private ArrayList<Node> nodes = new ArrayList<Node>();

    private User user = new User();

    private String server_url;

    /**
     * into smartconfig state
     */
    private Boolean configState;

    /**
     * login state
     */
    private Boolean loginState;

    public Boolean getLoginState() {
        return loginState;
    }

    public void setLoginState(Boolean loginState) {
        this.loginState = loginState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("loginState", loginState);
        editor.apply();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", user.email);
        editor.putString("userToken", user.user_key);
        editor.apply();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("server_url", server_url);
        editor.apply();
    }

    public Boolean getConfigState() {
        return configState;
    }

    public void setConfigState(Boolean configState) {
        this.configState = configState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("configState", configState);
        editor.apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = this.getSharedPreferences("IOT", Context.MODE_PRIVATE);
        sp.getString("serverAddress", "http://192.168.21.83:8080/v1");
        user.email = sp.getString("userName", "awong1900@163.com");
        user.user_key = sp.getString("userToken", "sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");

        server_url = sp.getString("server_url", "https://iot.seeed.cc/v1");

        configState = sp.getBoolean("configState", false);

        configState = sp.getBoolean("loginState", false);

        init();

        getGrovesData();

        getNodesData();
    }

    private void init() {
        IotApi.SetServerUrl(server_url);
    }

    public void getGrovesData() {
        IotApi api = new IotApi();
        String token = user.user_key;
        api.setAccessToken(token);
        IotService iot = api.getService();
        iot.scanDrivers(new Callback<List<GroverDriver>>() {
            @Override
            public void success(List<GroverDriver> groverDrivers, retrofit.client.Response response) {
                for (GroverDriver groveDriver : groverDrivers) {
                    groveDriver.save();
//                    new DownloadImageAsyncTask().execute(groveDriver);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());
            }
        });
    }

    public void getNodesData() {
        IotApi api = new IotApi();
        String token = user.user_key;
        api.setAccessToken(token);
        final IotService iot = api.getService();
        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                if (nodeListResponse.status.equals("200")) {
                    nodes = (ArrayList) nodeListResponse.nodes;
                    for (Node node : nodes) {
                        node.save();
                    }
                } else {
                    Log.e(getClass().getName(), nodeListResponse.msg);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());
            }
        });
    }


    class DownloadImageAsyncTask extends AsyncTask<GroverDriver, String, Void> {

        @Override
        protected Void doInBackground(GroverDriver... groverDrivers) {
            Bitmap bitmap = null;
            for (GroverDriver groverDriver : groverDrivers) {
                try {
                    URL imageUrl = groverDriver.ImageURL;
                    imageUrl.openConnection();
                    bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.toString());
                }

                saveToInternalSorage(bitmap, Integer.toString(groverDriver.ID));
            }

            return null;
        }
    }

    private String saveToInternalSorage(Bitmap bitmapImage, String fileName) {
        final String DIR = "grove";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(DIR, Context.MODE_PRIVATE);
        Log.e(getClass().getName(), directory.getPath());
        // Create imageDir
        File file = new File(directory, fileName);

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(file);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
}
