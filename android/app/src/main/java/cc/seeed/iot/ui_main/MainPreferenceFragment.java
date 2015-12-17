package cc.seeed.iot.ui_main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.util.Common;

/**
 * Created by tenwong on 15/12/14.
 */
public class MainPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private final static String TAG = "MainPreferenceFragment";

    public final static String PREF_OTA_SERVER = "pref_ota_server";
    public final static String PREF_CHANGE_PWD = "pref_change_pwd";

    private Context context;
    private String ota_ip;
    private String ota_url;

    private EditTextPreference ep_server;
    private Preference ep_pwd;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preference);
        context = getActivity().getApplication();
        ota_ip = ((MyApplication) getActivity().getApplication()).getOtaServerIP();
        ota_url = ((MyApplication) getActivity().getApplication()).getOtaServerUrl();


        ep_server = (EditTextPreference) findPreference(PREF_OTA_SERVER);
        ep_pwd = (Preference) findPreference(PREF_CHANGE_PWD);

        initView();
    }

    private void initView() {
        if (ota_ip.equals(Common.OTA_CHINA_IP)) {
            ep_server.setSummary(ota_url + " (China)");
        } else if (ota_ip.equals(Common.OTA_INTERNATIONAL_IP)) {
            ep_server.setSummary(ota_url + " (International)");
        } else {
            ep_server.setSummary(ota_ip + " (Custom)");
        }

        ep_pwd.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case PREF_CHANGE_PWD:
                Intent intent = new Intent(getActivity(), ChangePwdActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

//    private void initView() {
//        ep_name.setSummary(node.name);
//        ep_name.setText(node.name);
//        ep_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                ep_name.setSummary((String) newValue);
//                ep_name.setText((String) newValue);
//                nodeRename(node, (String) newValue, ep_name);
//                return true;
//            }
//        });
//
//
//        if (node.dataxserver.equals(ota_ip)) {
//            sp_server.setChecked(false);
//        } else {
//            sp_server.setChecked(true);
//        }
//        sp_server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (ep_ip.getText().equals(ota_ip))
//                    return true;
//
//                if (!(Boolean) newValue) {
//                    nodeXserverIp(node, ota_ip, sp_server);
//                }
//
//                return true;
//            }
//        });
//
//        ep_ip.setSummary(node.dataxserver);
//        ep_ip.setText(node.dataxserver);
//        ep_ip.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                String ip = (String) newValue;
//                if (!validate(ip)) {
//                    notifyFail("IP Address is invalid!");
//                    return false;
//                }
//                ep_ip.setSummary(ip);
//                ep_ip.setText(ip);
//                nodeXserverIp(node, ip, ep_ip);
//                return true;
//            }
//        });
//    }
//
//    private void nodeXserverIp(final Node node, String newValue, final EditTextPreference ep_ip) {
//        ep_ip.setEnabled(false);
//        IotApi api = new IotApi();
//        api.setAccessToken(node.node_key);
//        final IotService iot = api.getService();
//        iot.nodeSettingDataxserver(newValue, new Callback<CommonResponse>() {
//            @Override
//            public void success(CommonResponse commonResponse, Response response) {
//                ep_ip.setEnabled(true);
//                if (!commonResponse.status.equals("200")) {
//                    ep_ip.setSummary(node.dataxserver);
//                    ep_ip.setText(node.dataxserver);
//                    notifyFail(commonResponse.msg);
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e(TAG, "Change xchange ip failure!");
//                ep_ip.setEnabled(true);
//                ep_ip.setSummary(node.dataxserver);
//                ep_ip.setText(node.dataxserver);
//                notifyFail("Change xchange ip failure!");
//            }
//        });
//    }
//
//    private void nodeXserverIp(final Node node, String newValue, final SwitchPreference sp_server) {
//        sp_server.setEnabled(false);
//        IotApi api = new IotApi();
//        api.setAccessToken(node.node_key);
//        final IotService iot = api.getService();
//        iot.nodeSettingDataxserver(newValue, new Callback<CommonResponse>() {
//            @Override
//            public void success(CommonResponse commonResponse, Response response) {
//                sp_server.setEnabled(true);
//                if (!commonResponse.status.equals("200")) {
//                    sp_server.setChecked(true);
//                    notifyFail(commonResponse.msg);
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e(TAG, "Change xchange ip failure!");
//                sp_server.setEnabled(true);
//                sp_server.setChecked(true);
//                notifyFail("Change xchange ip failure!");
//            }
//        });
//    }
//
//    private void nodeRename(final Node node, String newName, final EditTextPreference ep_name) {
//        ep_name.setEnabled(false);
//        IotApi api = new IotApi();
//        User user = ((MyApplication) getActivity().getApplication()).getUser();
//        api.setAccessToken(user.user_key);
//        final IotService iot = api.getService();
//        iot.nodesRename(newName, node.node_sn, new Callback<NodeResponse>() {
//            @Override
//            public void success(NodeResponse nodeResponse, Response response) {
//                ep_name.setEnabled(true);
//                if (!nodeResponse.status.equals("200")) {
//                    ep_ip.setSummary(node.dataxserver);
//                    ep_ip.setText(node.dataxserver);
//                    notifyFail(nodeResponse.msg);
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e(TAG, "Rename wio link failure!");
//                ep_name.setEnabled(true);
//                ep_name.setSummary(node.name);
//                ep_name.setText(node.name);
//                notifyFail("Rename wio link failure!");
//            }
//        });
//    }
//
//    private void notifyFail(String msg) {
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
//    }
//
//    private boolean validate(String ip) {
//        boolean valid = true;
//
//        if (ip.isEmpty() || !Patterns.IP_ADDRESS.matcher(ip).matches()) {
//            valid = false;
//        }
//
//        return valid;
//    }
}
