package cc.seeed.iot.ui_main;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import cc.seeed.iot.R;

public class AboutActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    TextView aboutBodyView;
    TextView nameAndVersionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("About");

        aboutBodyView = (TextView) findViewById(R.id.about_body);
        nameAndVersionView = (TextView) findViewById(R.id.app_name_and_version);
        initView();
    }

    private void initView() {

        String versionName = getVersionName();
        nameAndVersionView.setText(Html.fromHtml(getString(R.string.title_about, versionName)));

        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());
    }

    private String getVersionName() {
        String VERSION_UNAVAILABLE = "N/A";
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }

        return versionName;
    }

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
}
