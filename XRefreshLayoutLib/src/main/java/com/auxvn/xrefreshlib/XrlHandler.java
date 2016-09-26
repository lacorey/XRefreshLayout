package com.auxvn.xrefreshlib;

import android.view.View;

/**
 * Created by zhaoxin on 16/9/24.
 */

public interface XrlHandler {
    public boolean checkCanDoRefresh(final XRefreshLayout frame, final View content, final View header);

    public void onRefreshBegin(final XRefreshLayout frame);
}
