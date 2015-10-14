package cc.seeed.iot.ui_main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.ui_main.QrGen.Contents;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.ui_main.QrGen.QRCodeEncoder;

public class NodeDetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageView mQrImageView;
    private TextView mUrlTextView;

    private Node node;
    private List<Node> nodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Node Detail");

        mQrImageView = (ImageView) findViewById(R.id.qr_image);
        mUrlTextView = (TextView) findViewById(R.id.qr_url);

        init();
        initView();
    }

    private void init() {
        node = new Node();
        nodes = new ArrayList<Node>();
        nodes = ((MyApplication) NodeDetailActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", -1); //todo: check -1?
        node = nodes.get(position);
    }


    private void initView() {
        String server_url = "https://iot.seeed.cc/v1/node/resources?"; //todo, changeable server url;
        String node_key = node.node_key;
        String url = server_url + "access_token=" + node_key;
        Log.i("iot", "Url:" + url);

//        Bitmap myBitmap = QRCode.from(url).bitmap();
//        mQrImageView.setImageBitmap(myBitmap);

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(url, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 500);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            mQrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        mUrlTextView.setText(url);
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
