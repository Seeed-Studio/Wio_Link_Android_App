package cc.seeed.iot.ui_main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.lucky.indexablelistview.util.ContentAdapter;
import com.lucky.indexablelistview.widget.IndexableListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.adapter.GrovesAdapter;
import cc.seeed.iot.ui_main.util.DividerItemDecoration;
import cc.seeed.iot.util.ComparatorUtils;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.webapi.model.GroverDriver;

public class GrovesActivity extends AppCompatActivity {
    private static String TAG = "GrovesActivity";
    public Toolbar mToolbar;
    IndexableListView mRecyclerView;
    GrovesAdapter mAdapter;
    List<GroverDriver> groverDrivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groves);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.groves);

        mRecyclerView = (IndexableListView) findViewById(R.id.groves);


        groverDrivers = DBHelper.getGrovesAll();
        Collections.sort(groverDrivers, new ComparatorUtils.ComparatorName());
        initView();
    }

    private void initView() {

        if (mRecyclerView != null) {
          /*  mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layout = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layout);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));*/
            mAdapter = new GrovesAdapter(this, groverDrivers);
        //    mAdapter = new ContentAdapter(this,android.R.layout.simple_list_item_1, mItems);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setFastScrollEnabled(true);
        }
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

   /* public class GrovesRecyclerAdapter extends RecyclerView.Adapter<GrovesRecyclerAdapter.MainViewHolder> {
        private List<GroverDriver> groves;

        public GrovesRecyclerAdapter(List<GroverDriver> groves) {
            this.groves = new ArrayList<>();
            this.groves = groves;
        }

        @Override
        public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grove, parent, false);

            return new MainViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MainViewHolder holder, final int position) {
            GroverDriver grove = groves.get(position);
            ImageView grove_image = holder.grove_image;
            UrlImageViewHelper.setUrlDrawable(grove_image, grove.ImageURL, R.mipmap.grove_default,
                    UrlImageViewHelper.CACHE_DURATION_INFINITE);
//            String name = grove.GroveName.replaceFirst("Grove[\\s_-]+", "");
            holder.mGroveNameView.setText(grove.GroveName);
            String interfaceType = grove.InterfaceType;
            if(interfaceType.equals("GPIO"))
                interfaceType = "Digital";
            holder.mGroveInterfaceView.setText(interfaceType);

        }

        public void updateAll(List<GroverDriver> groverDrivers) {
            this.groves = groverDrivers;
            notifyDataSetChanged();

        }

        @Override
        public int getItemCount() {
            return groves.size();
        }


        public class MainViewHolder extends RecyclerView.ViewHolder {
            ImageView grove_image;
            TextView mGroveNameView;
            TextView mGroveInterfaceView;
            View mView;

            public MainViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                grove_image = (ImageView) itemView.findViewById(R.id.image);
                mGroveNameView = (TextView) itemView.findViewById(R.id.name);
                mGroveInterfaceView = (TextView) itemView.findViewById(R.id.interface_type);
            }

        }
    }*/
}
