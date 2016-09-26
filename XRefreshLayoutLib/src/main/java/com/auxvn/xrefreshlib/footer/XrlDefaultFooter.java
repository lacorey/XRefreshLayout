package com.auxvn.xrefreshlib.footer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.auxvn.xrefreshlib.R;
import com.auxvn.xrefreshlib.XrlFooterHandler;

/**
 * Created by zhaoxin on 16/9/24.
 */

public class XrlDefaultFooter extends FrameLayout implements XrlFooterHandler{

    public XrlDefaultFooter(Context context) {
        super(context);
        initFooterView();
    }

    public XrlDefaultFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFooterView();
    }

    public XrlDefaultFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFooterView();
    }

    public void initFooterView(){
        View footer = LayoutInflater.from(getContext()).inflate(R.layout.xrl_default_footer, this);
    }

    @Override
    public void onUILoadMore() {

    }

    @Override
    public void onUILoadMoreComplete() {

    }

    @Override
    public void onUILoadMoreError() {

    }
}
