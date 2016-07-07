package cc.seeed.iot.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.lucky.indexablelistview.widget.IndexableListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.R;
import cc.seeed.iot.adapter.GrovesAdapter;
import cc.seeed.iot.adapter.set_node.GroveFilterRecyclerAdapter;
import cc.seeed.iot.util.ComparatorUtils;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontEditView;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.view.QuickReturnListView;
import cc.seeed.iot.webapi.model.GroverDriver;

public class GrovesActivity extends BaseActivity implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener {
    private static String TAG = "GrovesActivity";
    GrovesAdapter mAdapter;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mRvGroveFilter)
    RecyclerView mRvGroveFilter;
    @InjectView(R.id.groves)
    QuickReturnListView mLvGroves;
    @InjectView(R.id.mRlSearch)
    RelativeLayout mRlSearch;
    View mHeader;

    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private List<GroverDriver> mGroveDrivers;

    private boolean isChickTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groves);
        ButterKnife.inject(this);

        mGroveDrivers = DBHelper.getGrovesAll();
        Collections.sort(mGroveDrivers, new ComparatorUtils.ComparatorName());
        while (true) {
            if (mGroveDrivers == null || mGroveDrivers.size() == 0) {
                break;
            }
            int num = (int) ToolUtil.getSimpleName(mGroveDrivers.get(0).GroveName).charAt(0);
            if ((num >= 'a' && num <= 'z') || (num >= 'A' && num <= 'Z')) {
                break;
            } else {
                mGroveDrivers.add(mGroveDrivers.size(), mGroveDrivers.get(0));
                mGroveDrivers.remove(0);
            }
        }
        initView();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.groves);
        // init();
        init();
        if (mLvGroves != null) {
            mAdapter = new GrovesAdapter(this, mGroveDrivers);
            mLvGroves.setAdapter(mAdapter);
            mLvGroves.setFastScrollEnabled(true);
            updateGroveListAdapter(mGroveDrivers);
        }

        if (mRvGroveFilter != null) {
            mRvGroveFilter.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRvGroveFilter.setLayoutManager(layoutManager);
            mGroveTypeListAdapter = new GroveFilterRecyclerAdapter(Constant.groveTypes);
            mGroveTypeListAdapter.setOnItemClickListener(this);
            mRvGroveFilter.setAdapter(mGroveTypeListAdapter);
            mGroveTypeListAdapter.updateSelection(0);
        }

    }

    private int mCachedVerticalScrollRange;
    private int mQuickReturnHeight;
    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    private int mState = STATE_ONSCREEN;
    private int mScrollY;
    private int mMinRawY = 0;
    private TranslateAnimation anim;
    private View mPlaceHolder;

    //初始化搜索框的滑动显示和隐藏
    private void init() {
        mHeader = getLayoutInflater().inflate(R.layout.header, null);
        mPlaceHolder = mHeader.findViewById(R.id.placeholder);
        mLvGroves.addHeaderView(mHeader);
        mLvGroves.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mQuickReturnHeight = mRlSearch.getHeight();
                        mLvGroves.computeScrollY();
                        mCachedVerticalScrollRange = mLvGroves.getListHeight();
                        if (isChickTag) {
                            isChickTag = false;
                        }
                    }
                });

        mLvGroves.setOnScrollListener(new AbsListView.OnScrollListener() {
            @SuppressLint("NewApi")
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                mScrollY = 0;
                int translationY = 0;

                if (mLvGroves.scrollYIsComputed()) {
                    mScrollY = mLvGroves.getComputedScrollY();
                }
                int rawY;
                if (isChickTag) {
                    rawY = 0;
                } else {
                    rawY = mPlaceHolder.getTop()
                            - Math.min(
                            mCachedVerticalScrollRange - mLvGroves.getHeight(), mScrollY);
                }

                switch (mState) {
                    case STATE_OFFSCREEN:
                        if (rawY <= mMinRawY) {
                            mMinRawY = rawY;
                        } else {
                            mState = STATE_RETURNING;
                        }
                        translationY = rawY;
                        break;

                    case STATE_ONSCREEN:
                        if (rawY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        translationY = rawY;
                        break;

                    case STATE_RETURNING:
                        translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                        if (translationY > 0) {
                            translationY = 0;
                            mMinRawY = rawY - mQuickReturnHeight;
                        }

                        if (rawY > 0) {
                            mState = STATE_ONSCREEN;
                            translationY = rawY;
                        }

                        if (translationY < -mQuickReturnHeight) {
                            mState = STATE_OFFSCREEN;
                            mMinRawY = rawY;
                        }
                        break;
                }

                /** this can be used if the build is below honeycomb **/
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    anim = new TranslateAnimation(0, 0, translationY,
                            translationY);
                    anim.setFillAfter(true);
                    anim.setDuration(0);
                    mRlSearch.startAnimation(anim);
                } else {
                    mRlSearch.setTranslationY(translationY);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
    }

    public void showSearchPopWindow(Activity activity) {

        View view = LayoutInflater.from(activity).inflate(R.layout.popwindow_search, null);
        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });

        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        final FontTextView mTvNoReault = (FontTextView) view.findViewById(R.id.mTvNoReault);
        RelativeLayout mRlSearch = (RelativeLayout) view.findViewById(R.id.mRlSearch);
        RelativeLayout mRlClear = (RelativeLayout) view.findViewById(R.id.mRlClear);
        RelativeLayout mRlAction = (RelativeLayout) view.findViewById(R.id.mRlAction);
        final FontEditView mEtSearch = (FontEditView) view.findViewById(R.id.mEtSearch);
        final ListView mLvResult = (ListView) view.findViewById(R.id.mLvResult);
        final GrovesAdapter adapter = new GrovesAdapter(activity, new ArrayList<GroverDriver>(), true);
        mLvResult.setAdapter(adapter);
        mEtSearch.setFocusable(true);
        mEtSearch.setFocusableInTouchMode(true);
        mEtSearch.requestFocus();
//        imm.showSoftInputFromInputMethod(mEtSearch.getWindowToken(), 0);
//        imm.toggleSoftInputFromWindow(mEtSearch.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
        showSoftInput(mEtSearch);

        mRlAction.setOnClickListener(null);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });

        mLvResult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                hideKeyboard(view);
                return false;
            }
        });

        mRlClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtSearch.setText("");
            }
        });

        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mTvNoReault.setVisibility(View.GONE);
                    mLvResult.setVisibility(View.GONE);
                } else {
                    List<GroverDriver> groves = DBHelper.getSearchGroves(s.toString());
                    if (groves.size() == 0) {
                        mTvNoReault.setVisibility(View.VISIBLE);
                        mLvResult.setVisibility(View.GONE);
                    } else {
                        mTvNoReault.setVisibility(View.GONE);
                        mLvResult.setVisibility(View.VISIBLE);
                    }
                    adapter.updateAll(groves);
                }
            }
        });
        //  mLLMenuContainer.setBackgroundResource(R.drawable.withe_shadow_bg);

        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(new View(activity), Gravity.NO_GRAVITY, 0, 0);
//        popWindow.showAsDropDown(targetView);
    }

    public void showSoftInput(final EditText editText) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 200);
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

    @OnClick(R.id.mRlSearch)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mRlSearch:
                showSearchPopWindow(GrovesActivity.this);
                break;
        }
    }

    @Override
    public void onItemClick(View view, int postion) {
        isChickTag = true;
        String groveType = Constant.groveTypes[postion];

        List<GroverDriver> inputGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> outputGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> gpioGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> analogGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> uartGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> i2cGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> eventGroves = new ArrayList<GroverDriver>();


        if (mGroveDrivers == null)
            return;

        for (GroverDriver g : mGroveDrivers) {
            if (!g.Writes.isEmpty()) {
                outputGroves.add(g);
            }
            if (!g.Reads.isEmpty()) {
                inputGroves.add(g);
            }
            if (g.HasEvent) {
                eventGroves.add(g);
            }
            switch (g.InterfaceType) {
                case "GPIO":
                    gpioGroves.add(g);
                    break;
                case "ANALOG":
                    analogGroves.add(g);
                    break;
                case "UART":
                    uartGroves.add(g);
                    break;
                case "I2C":
                    i2cGroves.add(g);
                    break;
            }
        }

        mGroveTypeListAdapter.updateSelection(postion);

        if (groveType.equals("All")) {
            updateGroveListAdapter(mGroveDrivers);
        } else if (groveType.equals("Input")) {
            updateGroveListAdapter(inputGroves);
        } else if (groveType.equals("Output")) {
            updateGroveListAdapter(outputGroves);
        } else if (groveType.equals("GPIO")) {
            updateGroveListAdapter(gpioGroves);
        } else if (groveType.equals("Analog")) {
            updateGroveListAdapter(analogGroves);
        } else if (groveType.equals("UART")) {
            updateGroveListAdapter(uartGroves);
        } else if (groveType.equals("I2C")) {
            updateGroveListAdapter(i2cGroves);
        } else if (groveType.equals("Event")) {
            updateGroveListAdapter(eventGroves);
        }

    }

    private void updateGroveListAdapter(List<GroverDriver> groverDrivers) {
        mLvGroves.setAdapter(mAdapter);
        mAdapter.updateAll(groverDrivers);
        //     mRlSearch.setTranslationY(mLvGroves.getTop());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
