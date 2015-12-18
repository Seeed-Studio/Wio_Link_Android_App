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
}
