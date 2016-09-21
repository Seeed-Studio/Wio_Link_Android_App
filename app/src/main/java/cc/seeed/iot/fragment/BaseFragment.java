package cc.seeed.iot.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import cc.seeed.iot.logic.CmdConst;
import cc.seeed.iot.mgr.IUiObserver;
import cc.seeed.iot.mgr.UiObserverManager;


/**
 *
 * @描述: 基类的fragment
 * Created by Jerry on 2015/7/13.
 */
public abstract class BaseFragment extends Fragment implements IUiObserver,CmdConst
{
	// 获取宿主,也就是当前的activity
	protected FragmentActivity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//获取宿主,也就是当前的activity
		mActivity = getActivity();
		UiObserverManager.getInstance().registerEvent(monitorEvents(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return initView(inflater,container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		initData();
	}

	protected abstract View initView(LayoutInflater inflater, ViewGroup container);

	/**
	 * 如果子类需要加载数据，就复写此方法
	 */
	protected void initData()
	{

	}

	public String[] monitorEvents() {
		return new String[]{""};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		UiObserverManager.getInstance().unregisterEvent(monitorEvents(), this);
	}

	public void hideKeyboard(View view) {
		InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onEvent(String event, boolean ret, String errInfo, Object[] data) {

	}

	@Override
	public void onEvent(String event, int ret, String errInfo, Object[] data) {

	}
}
