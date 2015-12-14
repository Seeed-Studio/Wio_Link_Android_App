package cc.seeed.iot.ui_main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

public class NodeSettingActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public Node node;

    //    TextView aboutBodyView;
//    TextView nameAndVersionView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_about);
//
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Node Setting");
//
//        aboutBodyView = (TextView) findViewById(R.id.about_body);
//        nameAndVersionView = (TextView) findViewById(R.id.app_name_and_version);
//        initView();
//    }
//
//    private void initView() {
//
//        String versionName = getVersionName();
//        nameAndVersionView.setText(Html.fromHtml(getString(R.string.title_about, versionName)));
//
//        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
//        aboutBodyView.setMovementMethod(new LinkMovementMethod());
//    }
//
//    private String getVersionName() {
//        String VERSION_UNAVAILABLE = "N/A";
//        PackageManager pm = getPackageManager();
//        String packageName = getPackageName();
//        String versionName;
//        try {
//            PackageInfo info = pm.getPackageInfo(packageName, 0);
//            versionName = info.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            versionName = VERSION_UNAVAILABLE;
//        }
//
//        return versionName;
//    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Node Setting");

        String node_sn = getIntent().getStringExtra("node_sn");

        Bundle bundle = new Bundle();
        bundle.putString("node_sn", node_sn);
        NodePreferenceFragment fragment = new NodePreferenceFragment();
        fragment.setArguments(bundle);

        String tag = node_sn;
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }


}
