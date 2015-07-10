package cc.seeed.iot.ui_setnode;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.Node;

public class SetupIotNodeActivity extends AppCompatActivity {
    public Toolbar toolbar;
    ArrayList<Node> nodes;
    Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nodes = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", 1);
        node = nodes.get(position);
        Snackbar.make(toolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setup_iot_node, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.update) {
            //TODO update firmware
            Snackbar.make(toolbar, "Here's a Snackbar" + node.name, Snackbar.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
