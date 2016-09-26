package com.auxvn.xrefreshlib;

import android.content.Context;
import android.util.AttributeSet;

import com.auxvn.xrefreshlib.footer.XrlDefaultFooter;
import com.auxvn.xrefreshlib.header.XrlDefaultHeader;

/**
 * Created by zhaoxin on 16/9/24.
 */

public class XRefreshDefautLayout extends XRefreshLayout{
    private XrlDefaultHeader mHeader;
    private XrlDefaultFooter mFooter;

    public XRefreshDefautLayout(Context context) {
        super(context);
        initViews();
    }

    public XRefreshDefautLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public XRefreshDefautLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        mHeader = new XrlDefaultHeader(getContext());
        setHeaderView(mHeader);
        mFooter = new XrlDefaultFooter(getContext());
        setFooterView(mFooter);
//        addPtrUIHandler(mPtrClassicHeader);
    }

    public XrlDefaultHeader getHeader() {
        return mHeader;
    }

    public XrlDefaultFooter getFooter() {
        return mFooter;
    }

    /**
     * Specify the last update time by this key string
     *
     * @param key
     */
    public void setLastUpdateTimeKey(String key) {
        if (mHeader != null) {
            mHeader.setLastUpdateTimeKey(key);
        }
    }

    /**
     * Using an object to specify the last update time.
     *
     * @param object
     */
    public void setLastUpdateTimeRelateObject(Object object) {
        if (mHeader != null) {
            mHeader.setLastUpdateTimeRelateObject(object);
        }
    }
}
