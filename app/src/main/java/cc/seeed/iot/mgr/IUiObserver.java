package cc.seeed.iot.mgr;

/**
 * Created by zjm on 2014/11/9.
 */
public interface IUiObserver {
    void onEvent(String event, boolean ret, String errInfo, Object[] data);
    void onEvent(String event, int ret, String errInfo, Object[] data);
}
