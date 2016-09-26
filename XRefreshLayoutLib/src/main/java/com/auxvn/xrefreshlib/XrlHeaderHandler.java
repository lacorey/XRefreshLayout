package com.auxvn.xrefreshlib;

/**
 * Created by zhaoxin on 16/9/24.
 */

public interface XrlHeaderHandler {
    /**
     * When the content view has reached top and refresh has been completed, view will be reset.
     *
     * @param frame
     */
    public void onUIReset(XRefreshLayout frame);

    /**
     * prepare for loading
     *
     * @param frame
     */
    public void onUIRefreshPrepare(XRefreshLayout frame);

    /**
     * perform refreshing UI
     */
    public void onUIRefreshBegin(XRefreshLayout frame);

    /**
     * perform UI after refresh
     */
    public void onUIRefreshComplete(XRefreshLayout frame);

//    public void onUIPositionChange(XRefreshLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator);
}
