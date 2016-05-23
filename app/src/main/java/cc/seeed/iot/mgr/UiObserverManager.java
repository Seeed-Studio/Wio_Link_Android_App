package cc.seeed.iot.mgr;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zjm on 2014/11/9.
 */
public class UiObserverManager {
    String TAG = "UiObserverManager: ";
    static UiObserverManager sIns;

    public static UiObserverManager getInstance() {
        if (sIns == null) {
            synchronized (UiObserverManager.class) {
                if (sIns == null) {
                    sIns = new UiObserverManager();
                }
            }
        }
        return sIns;
    }
    HashMap<String,List<IUiObserver>> observers = new HashMap<>();

    public void registerEvent(String e,IUiObserver observer) {
        if (observers.containsKey(e)) {
            List<IUiObserver> obs = observers.get(e);
            if (!obs.contains(observer))
                obs.add(observer);
        } else {
            List<IUiObserver> obs = new LinkedList<IUiObserver>();
            if (!obs.contains(observer))
                obs.add(observer);
            observers.put(e,obs);
        }
    }

    public void registerEvent(String[] es,IUiObserver observer) {
        if (es != null) {
            for (String e:es) {
                registerEvent(e,observer);
            }
        }
    }

    public void unregisterEvent(String e,IUiObserver observer) {
        if (observers.containsKey(e)) {
            List<IUiObserver> obs = observers.get(e);
            obs.remove(observer);
        }
    }

    public void unregisterEvent(String[] es,IUiObserver observer) {
        if (es != null) {
            for (String e:es) {
                unregisterEvent(e,observer);
            }
        }
    }


    public void dispatchEvent(final String e,final boolean ret,final String errInfo,final Object[] data) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            dispatchEventOnUiThread(e,ret,errInfo,data);
        } else {
            ThreadManager.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dispatchEventOnUiThread(e,ret,errInfo,data);
                }
            },0);
        }
    }
    public void dispatchEvent(final String e,final int ret,final String errInfo,final Object[] data) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            dispatchEventOnUiThread(e,ret,errInfo,data);
        } else {
            ThreadManager.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dispatchEventOnUiThread(e,ret,errInfo,data);
                }
            },0);
        }
    }

    void dispatchEventOnUiThread(String e,int ret,String errInfo,Object[] data) {
        if(TextUtils.isEmpty(errInfo))errInfo="请求失败";
        if (observers.containsKey(e)) {
            List<IUiObserver> obs = observers.get(e);
            List<IUiObserver> copy = new ArrayList<>(obs);
            for (IUiObserver ob : copy) {
                Log.d(TAG, "cmd: " + e);
                ob.onEvent(e,ret,errInfo,data);
            }
        }
    }
    void dispatchEventOnUiThread(String e,boolean ret,String errInfo,Object[] data) {
        if(TextUtils.isEmpty(errInfo))errInfo="请求失败";
        if (observers.containsKey(e)) {
            List<IUiObserver> obs = observers.get(e);
            List<IUiObserver> copy = new ArrayList<>(obs);
            for (IUiObserver ob : copy) {
                Log.d(TAG, "cmd: " + e);
                ob.onEvent(e,ret,errInfo,data);
            }
        }
    }
}
