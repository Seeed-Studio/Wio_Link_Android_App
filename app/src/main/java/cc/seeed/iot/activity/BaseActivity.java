package cc.seeed.iot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import cc.seeed.iot.R;
import cc.seeed.iot.activity.user.LoginAndRegistActivity;
import cc.seeed.iot.logic.CmdConst;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.mgr.IUiObserver;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.ui_login.LoginActivity;
import cc.seeed.iot.util.ToolUtil;

/**
 * Created by seeed on 2016/2/18.
 */
public class BaseActivity extends AppCompatActivity implements IUiObserver, CmdConst {
    protected String SDPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiObserverManager.getInstance().registerEvent(monitorEvents(), this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (ToolUtil.isApkDebug()){
            View toolbar = findViewById(R.id.mToolbar);
            if (toolbar != null){
                toolbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(BaseActivity.this,TestActivity.class));
                    }
                });
            }
        }
    }

   /*  public void setTranslucentStatus()
     {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
         {
             Window win = getWindow();
             WindowManager.LayoutParams winParams = win.getAttributes();
             final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
             winParams.flags |= bits;
             win.setAttributes(winParams);
         }
         SystemStatusManager tintManager = new SystemStatusManager(this);
         tintManager.setStatusBarTintEnabled(true);
         tintManager.setStatusBarTintResource(0);//状态栏无背景
     }*/


    public String[] monitorEvents() {
        return new String[]{};
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UiObserverManager.getInstance().unregisterEvent(monitorEvents(), this);
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {

    }

    @Override
    public void onEvent(String event, int ret, String errInfo, Object[] data) {

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public boolean isLogin(){
        if (UserLogic.getInstance().isLogin()){
            return true;
        }else {
            startActivity(new Intent(this, LoginAndRegistActivity.class));
            return false;
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
}
