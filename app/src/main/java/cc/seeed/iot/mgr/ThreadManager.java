package cc.seeed.iot.mgr;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;


/**
 * Created by zjm on 2015/4/12.
 */
public class ThreadManager {
    static ThreadManager sIns;
    public static ThreadManager getInstance() {
        if (sIns == null) {
            synchronized (ThreadManager.class) {
                if (sIns == null) {
                    sIns = new ThreadManager();
                }
            }
        }
        return sIns;
    }
    Handler mUiHandler;
    Handler mWorkerHandler;
    HandlerThread mWorkerThread;
    Handler mDBHandler;
    HandlerThread mDBThread;
    private ThreadManager() {
        mUiHandler = new Handler(Looper.getMainLooper());
        mWorkerThread = new HandlerThread("worker");
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());
    }

    public void runOnUiThread(Runnable r,long delay) {
            mUiHandler.postDelayed(r, delay);
    }

    public void removeRunnableOnUIThread(Runnable r) {
        mUiHandler.removeCallbacks(r);
    }

    public void runOnWorkerThread(Runnable r,long delay) {
            mWorkerHandler.postDelayed(r, delay);
    }

    public void removeRunnableOnWorkerThread(Runnable r) {
        mWorkerHandler.removeCallbacks(r);
    }

    public Looper getWorkerLooper() {
        return mWorkerThread.getLooper();
    }

//    public void runOnDbThread(Runnable r,long delay) {
//
//    }
}
