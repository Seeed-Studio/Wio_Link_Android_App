package cc.seeed.iot.ui_main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.SuccessResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tenwong on 15/12/14.
 */
public class NodePreferenceFragment extends PreferenceFragment {
    private final static String TAG = "NodePreferenceFragment";
    public final static String PREF_NODE_NAME = "pref_node_name";
    public final static String PREF_XSERVER_DEFAULT = "pref_xserver_defult";
    public final static String PREF_XSERVER_URL = "pref_xserver_url";

    private Context context;
    private Node node;
    private String ota_url;
    private String ota_ip;

    private SharedPreferences.OnSharedPreferenceChangeListener myPrefChangeListener;

    private EditTextPreference ep_name;
    private EditTextPreference ep_url;
    private SwitchPreference sp_server;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.node_preference);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        context = getActivity().getApplication();
        String node_sn = getArguments().getString("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);
        ota_url = ((MyApplication) getActivity().getApplication()).getOtaServerUrl();
        ota_ip = ((MyApplication) getActivity().getApplication()).getOtaServerIP();
        if (node.dataxserver == null)
            node.dataxserver = ota_url;

        ep_name = (EditTextPreference) findPreference(PREF_NODE_NAME);
        ep_url = (EditTextPreference) findPreference(PREF_XSERVER_URL);
        sp_server = (SwitchPreference) findPreference(PREF_XSERVER_DEFAULT);

        initView();
    }

    private void initView() {
        ep_name.setSummary(node.name);
        ep_name.setText(node.name);
        ep_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ep_name.setSummary((String) newValue);
                ep_name.setText((String) newValue);
                nodeRename(node, (String) newValue, ep_name);
                return true;
            }
        });


        if (node.dataxserver.equals(ota_url)) {
            sp_server.setChecked(false);
        } else {
            sp_server.setChecked(true);
        }
        sp_server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ep_url.getText().equals(ota_url))
                    return true;

                if (!(Boolean) newValue) {
                    nodeXserverIp(node, ota_ip, ota_url, sp_server);
                }

                return true;
            }
        });

        ep_url.setSummary(node.dataxserver);
        ep_url.setText(node.dataxserver);
        ep_url.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String url = (String) newValue;
//                if (!validate(ip)) {
//                    notifyFail("IP Address is invalid!");
//                    return false;
//                }

                String ip = "";
                try {
                    InetAddress address = InetAddress.getByName(new URL(url).getHost());
                    ip = address.getHostAddress();
                } catch (UnknownHostException | MalformedURLException e) {
//                    e.printStackTrace();
                    notifyFail(e.getMessage());
                    return false;
                }

//                getHostAddress(ota_server_url, progressDialog);
                ep_url.setSummary(url);
                ep_url.setText(url);
                nodeXserverIp(node, ip, url, ep_url);
                return true;
            }
        });
    }

    private void nodeXserverIp(final Node node, String ip, String url, final EditTextPreference ep_ip) {
        ep_ip.setEnabled(false);
        IotApi api = new IotApi();
        api.setAccessToken(node.node_key);
        final IotService iot = api.getService();
        iot.nodeSettingDataxserver(ip, url, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response) {
                ep_ip.setEnabled(true);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Change xchange ip failure!");
                ep_ip.setEnabled(true);
                ep_ip.setSummary(node.dataxserver);
                ep_ip.setText(node.dataxserver);
                notifyFail("Change xchange ip failure!");
            }
        });
    }

    private void nodeXserverIp(final Node node, String ip, String url, final SwitchPreference sp_server) {
        sp_server.setEnabled(false);
        IotApi api = new IotApi();
        api.setAccessToken(node.node_key);
        final IotService iot = api.getService();
        iot.nodeSettingDataxserver(ip, url, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response) {
                sp_server.setEnabled(true);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Change xchange ip failure!");
                sp_server.setEnabled(true);
                sp_server.setChecked(true);
                notifyFail("Change xchange ip failure!");
            }
        });
    }

    private void nodeRename(final Node node, String newName, final EditTextPreference ep_name) {
        ep_name.setEnabled(false);
        IotApi api = new IotApi();
        User user = ((MyApplication) getActivity().getApplication()).getUser();
        api.setAccessToken(user.user_key);
        final IotService iot = api.getService();
        iot.nodesRename(newName, node.node_sn, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response) {
                ep_name.setEnabled(true);
//                if (!nodeResponse.status.equals("200")) {
//                    ep_ip.setSummary(node.dataxserver);
//                    ep_ip.setText(node.dataxserver);
//                    notifyFail(nodeResponse.msg);
//                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Rename wio link failure!");
                ep_url.setSummary(node.dataxserver);
                ep_url.setText(node.dataxserver);
                ep_name.setEnabled(true);
                ep_name.setSummary(node.name);
                ep_name.setText(node.name);
                notifyFail("Rename wio link failure!");
            }
        });
    }

    private void notifyFail(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private boolean validate(String ip) {
        boolean valid = true;

        if (ip.isEmpty() || !Patterns.IP_ADDRESS.matcher(ip).matches()) {
            valid = false;
        }

        return valid;
    }

    private String getHostAddress(final String ota_server_url) {
        String ip = "";
        try {
            InetAddress address = InetAddress.getByName(new URL(ota_server_url).getHost());
            ip = address.getHostAddress();
        } catch (UnknownHostException | MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        return ip;
    }

}
