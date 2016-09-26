package com.auxvn.xrefreshlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.auxvn.xrefreshlib.indicator.XrlIndicator;

/**
 * Created by zhaoxin on 16/9/24.
 */

public class XRefreshLayout extends ViewGroup {
    //pull status
    public final static byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public final static byte PTR_STATUS_PREPARE = 2;
    public final static byte PTR_STATUS_LOADING = 3;
    public final static byte PTR_STATUS_COMPLETE = 4;
    private static final boolean DEBUG_LAYOUT = true;
    public static boolean DEBUG = false;
    private static int ID = 1;
    protected final String LOG_TAG = "XRefreshLayout:" + ++ID;
    protected final String TOUCH = "TouchEvent";

    protected View mContent;
    private View mHeaderView;
    private View mFooterView;
    // optional config for define header and content in xml file
    private int mHeaderId = 0;
    private int mContainerId = 0;
    private int mFooterId = 0;

    // config
    private int mDurationToClose = 200;
    private int mDurationToCloseHeader = 1000;
    private boolean mKeepHeaderWhenRefresh = true;
    private boolean mPullToRefresh = false;

    // working parameters
    private int mPagingTouchSlop;
    private int mHeaderHeight;
    private int mFooterHeight;
    private boolean mDisableWhenHorizontalMove = false;
    private int mFlag = 0x00;

    // disable when detect moving horizontally
    private boolean mPreventForHorizontal = false;
    private MotionEvent mLastMoveEvent;
    private int mLoadingMinTime = 500;
    private long mLoadingStartTime = 0;
    private boolean mHasSendCancelEvent = false;

    //sroller
    private Scroller mScroller;
    private XrlIndicator mIndicator;
    private ScrollChecker mScrollChecker;
    private XrlHandler mXrlHandler;

    public XRefreshLayout(Context context) {
        this(context, null);
    }

    public XRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.XRefreshLayout, 0, 0);
        if (arr != null) {
            mScroller = new Scroller(context);
            mIndicator = new XrlIndicator();
            mScrollChecker = new ScrollChecker();

            mHeaderId = arr.getResourceId(R.styleable.XRefreshLayout_xrl_header, mHeaderId);
            mContainerId = arr.getResourceId(R.styleable.XRefreshLayout_xrl_content, mContainerId);
            mFooterId = arr.getResourceId(R.styleable.XRefreshLayout_xrl_footer, mContainerId);

//            mPtrIndicator.setResistance(
//                    arr.getFloat(R.styleable.PtrFrameLayout_ptr_resistance, mPtrIndicator.getResistance()));

            mDurationToClose = arr.getInt(R.styleable.XRefreshLayout_xrl_duration_to_close, mDurationToClose);
            mDurationToCloseHeader = arr.getInt(R.styleable.XRefreshLayout_xrl_duration_to_close_header, mDurationToCloseHeader);

//            float ratio = mPtrIndicator.getRatioOfHeaderToHeightRefresh();
//            ratio = arr.getFloat(R.styleable.PtrFrameLayout_ptr_ratio_of_header_height_to_refresh, ratio);
//            mPtrIndicator.setRatioOfHeaderHeightToRefresh(ratio);

            mKeepHeaderWhenRefresh = arr.getBoolean(R.styleable.XRefreshLayout_xrl_keep_header_when_refresh, mKeepHeaderWhenRefresh);

            mPullToRefresh = arr.getBoolean(R.styleable.XRefreshLayout_xrl_pull_to_fresh, mPullToRefresh);
            arr.recycle();
        }

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount > 3) {
            throw new IllegalStateException("XRefreshLayout can only contains 3 children");
        } else if (childCount == 3) {
            if (mHeaderId != 0 && mHeaderView == null) {
                mHeaderView = findViewById(mHeaderId);
            }
            if (mContainerId != 0 && mContent == null) {
                mContent = findViewById(mContainerId);
            }
            if (mFooterId != 0 && mFooterView == null) {
                mFooterView = findViewById(mFooterId);
            }

            // not specify header or content
            if (mContent == null || mHeaderView == null || mFooterView == null) {

                View child1 = getChildAt(0);
                View child2 = getChildAt(1);
                View child3 = getChildAt(2);
                if (child1 instanceof XrlHeaderHandler && child2 instanceof XrlFooterHandler) {
                    mHeaderView = child1;
                    mFooterView = child2;
                    mContent = child3;
                } else {
                    throw new IllegalStateException("First child must be XrlHeaderHandler,third child must be XrlFooterHandler");
                }
            }
        } else if (childCount == 1) {
            mContent = getChildAt(0);
        } else {
            TextView errorView = new TextView(getContext());
            errorView.setClickable(true);
            errorView.setTextColor(0xffff6600);
            errorView.setGravity(Gravity.CENTER);
            errorView.setTextSize(20);
            errorView.setText("The content view in XRefreshLayout is empty. Do you forget to specify its id in xml layout file?");
            mContent = errorView;
            addView(mContent);
        }
        if (mHeaderView != null) {
            mHeaderView.bringToFront();
        }
        super.onFinishInflate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // TODO: 16/9/24
//        if (mScrollChecker != null) {
//            mScrollChecker.destroy();
//        }
//
//        if (mPerformRefreshCompleteDelay != null) {
//            removeCallbacks(mPerformRefreshCompleteDelay);
//        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        DLog.d(LOG_TAG, "onMeasure frame: width: %s, height: %s, padding: %s %s %s %s",
                 getMeasuredWidth(),getMeasuredHeight(),
                getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());


        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            DLog.d(LOG_TAG, "onMeasure header, width: %s, height: %s", mHeaderView.getMeasuredWidth(),mHeaderHeight);
            mIndicator.setHeaderHeight(mHeaderHeight);
        }

        if (mContent != null) {
            measureContentView(mContent, widthMeasureSpec, heightMeasureSpec);
            ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            DLog.d(LOG_TAG, "onMeasure content, width: %s, height: %s, margin: %s %s %s %s",
                    getMeasuredWidth(), getMeasuredHeight(),
                    lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
//            DLog.d(LOG_TAG, "onMeasure, currentPos: %s, lastPos: %s, top: %s",
//                    mPtrIndicator.getCurrentPosY(), mPtrIndicator.getLastPosY(), mContent.getTop());
        }

        if(mFooterView != null){
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            DLog.d(LOG_TAG, "onMeasure footer, width: %s, height: %s", mFooterView.getMeasuredWidth(),mFooterHeight);
        }
    }

    private void measureContentView(View child,
                                    int parentWidthMeasureSpec,
                                    int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

        DLog.d(LOG_TAG, "measureContentView, paddingLeft: %s, paddingRight: %s, lp.leftMargin: %s, lp.rightMargin: %s, lp.width: %s, lp.height: %s",
                getPaddingLeft(), getPaddingRight(),
                lp.leftMargin, lp.rightMargin, lp.width, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();
    }

    private void layoutChildren() {
        // TODO: 16/9/24 set right offset
        int offset = 0;
//        int offset = mPtrIndicator.getCurrentPosY();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            // enhance readability(header is layout above screen when first init)
            final int top = -(mHeaderHeight - paddingTop - lp.topMargin - offset);
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left,top,right,bottom);
            DLog.d(LOG_TAG, "onLayout header: %s %s %s %s", left, top, right, bottom);
        }
        if (mContent != null) {
//            if (isPinContent()) {
//                offset = 0;
//            }
            MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offset;
            final int right = left + mContent.getMeasuredWidth();
            final int bottom = top + mContent.getMeasuredHeight();
            DLog.d(LOG_TAG, "onLayout content: %s %s %s %s", left, top, right, bottom);
            mContent.layout(left, top, right, bottom);
        }
        if (mFooterView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            //hide the footer
            final int top = paddingTop + getMeasuredHeight();
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + mFooterHeight;
            mFooterView.layout(left, top, right, bottom);
            DLog.d(LOG_TAG, "onLayout footer: %s %s %s %s", left, top, right, bottom);
        }
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!isEnabled() || mContent == null || mHeaderView == null) {
            return dispatchTouchEventSupper(e);
        }

        int x = (int) e.getX();
        int y = (int) e.getY();

        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIndicator.onRelease();
                if (mIndicator.hasLeftStartPosition()) {
                    DLog.d(LOG_TAG, "call onRelease when user release");
                    onRelease(false);
                    if (mIndicator.hasMovedAfterPressedDown()) {
//                        sendCancelEvent();
                        return true;
                    }
                    return dispatchTouchEventSupper(e);
                } else {
                    return dispatchTouchEventSupper(e);
                }
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mIndicator.onPressDown(e.getX(), e.getY());

                mScrollChecker.abortIfWorking();

                mPreventForHorizontal = false;
                // The cancel event will be sent once the position is moved.
                // So let the event pass to children.
                // fix #93, #102
                dispatchTouchEventSupper(e);
                return true;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = e;
                mIndicator.onMove(e.getX(), e.getY());
                float offsetX = mIndicator.getOffsetX();
                float offsetY = mIndicator.getOffsetY();

                if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY))) {
                    if (mIndicator.isInStartPosition()) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    return dispatchTouchEventSupper(e);
                }

                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                boolean canMoveUp = mIndicator.hasLeftStartPosition();

                boolean canMoveDown = mXrlHandler != null && mXrlHandler.checkCanDoRefresh(this, mContent, mHeaderView);
                DLog.v(LOG_TAG, "ACTION_MOVE: offsetY:%s, currentPos: %s, moveUp: %s, canMoveUp: %s," +
                        " moveDown: %s: canMoveDown: %s", offsetY, mIndicator.getCurrentPosY(), moveUp, canMoveUp, moveDown, canMoveDown);

                // disable move when header not reach top
                if (moveDown && mXrlHandler != null && !mXrlHandler.checkCanDoRefresh(this, mContent, mHeaderView)) {
                    return dispatchTouchEventSupper(e);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    movePos(offsetY);
                    return true;
                }
        }
        return dispatchTouchEventSupper(e);
    }

    private void movePos(float deltaY) {
        // has reached the top
        if ((deltaY < 0 && mIndicator.isInStartPosition())) {
            DLog.e(LOG_TAG, String.format("has reached the top"));
            return;
        }

        int to = mIndicator.getCurrentPosY() + (int) deltaY;

        // over top
        if (mIndicator.willOverTop(to)) {
            DLog.e(LOG_TAG, String.format("over top"));
            to = mIndicator.POS_START;
        }

        mIndicator.setCurrentPos(to);
        int change = to - mIndicator.getLastPosY();
        updatePos(change);
    }

    private void updatePos(int change) {
        if (change == 0) {
            return;
        }

        boolean isUnderTouch = mIndicator.isUnderTouch();

        // once moved, cancel event will be sent to child
        if (isUnderTouch && !mHasSendCancelEvent && mIndicator.hasMovedAfterPressedDown()) {
            mHasSendCancelEvent = true;
//            sendCancelEvent();
        }

        // leave initiated position or just refresh complete
//        if ((mIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) ||
//                (mIndicator.goDownCrossFinishPosition() && mStatus == PTR_STATUS_COMPLETE && isEnabledNextPtrAtOnce())) {
        if ((mIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) ||
                (mIndicator.goDownCrossFinishPosition() && mStatus == PTR_STATUS_COMPLETE)) {

            mStatus = PTR_STATUS_PREPARE;
//            mPtrUIHandlerHolder.onUIRefreshPrepare(this);
            DLog.i(LOG_TAG, "PtrUIHandler: onUIRefreshPrepare, mFlag %s", mFlag);
        }

        // back to initiated position
//        if (mIndicator.hasJustBackToStartPosition()) {
//            tryToNotifyReset();
//
//            // recover event to children
//            if (isUnderTouch) {
//                sendDownEvent();
//            }
//        }
//
//        // Pull to Refresh
//        if (mStatus == PTR_STATUS_PREPARE) {
//            // reach fresh height while moving from top to bottom
//            if (isUnderTouch && !isAutoRefresh() && mPullToRefresh
//                    && mIndicator.crossRefreshLineFromTopToBottom()) {
//                tryToPerformRefresh();
//            }
//            // reach header height while auto refresh
//            if (performAutoRefreshButLater() && mIndicator.hasJustReachedHeaderHeightFromTopToBottom()) {
//                tryToPerformRefresh();
//            }
//        }

        DLog.v(LOG_TAG, "updatePos: change: %s, current: %s last: %s, top: %s, headerHeight: %s",
                change, mIndicator.getCurrentPosY(), mIndicator.getLastPosY(), mContent.getTop(), mHeaderHeight);

        mHeaderView.offsetTopAndBottom(change);
        mContent.offsetTopAndBottom(change);
        invalidate();

//        if (mPtrUIHandlerHolder.hasHandler()) {
//            mPtrUIHandlerHolder.onUIPositionChange(this, isUnderTouch, mStatus, mPtrIndicator);
//        }
//        onPositionChange(isUnderTouch, mStatus, mPtrIndicator);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public void setHeaderView(View header) {
        if (mHeaderView != null && header != null && mHeaderView != header) {
            removeView(mHeaderView);
        }
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            header.setLayoutParams(lp);
        }
        mHeaderView = header;
        addView(header);
    }

    public void setFooterView(View footer) {
        if (mFooterView != null && footer != null && mFooterView != footer) {
            removeView(mFooterView);
        }
        ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            footer.setLayoutParams(lp);
        }
        mFooterView = footer;
        addView(footer);
    }

    private void onRelease(boolean stayForLoading) {

        tryToPerformRefresh();

        if (mStatus == PTR_STATUS_LOADING) {
            // keep header for fresh
            if (mKeepHeaderWhenRefresh) {
                // scroll header back
                if (mIndicator.isOverOffsetToKeepHeaderWhileLoading() && !stayForLoading) {
                    mScrollChecker.tryToScrollTo(mIndicator.getOffsetToKeepHeaderWhileLoading(), mDurationToClose);
                } else {
                    // do nothing
                }
            } else {
                tryScrollBackToTopWhileLoading();
            }
        } else {
            if (mStatus == PTR_STATUS_COMPLETE) {
//                notifyUIRefreshComplete(false);
            } else {
//                tryScrollBackToTopAbortRefresh();
            }
        }
    }

    private boolean tryToPerformRefresh() {
        if (mStatus != PTR_STATUS_PREPARE) {
            return false;
        }

        //
        if ((mIndicator.isOverOffsetToKeepHeaderWhileLoading()) || mIndicator.isOverOffsetToRefresh()) {
            mStatus = PTR_STATUS_LOADING;
//            performRefresh();
        }
        return false;
    }

    /**
     * Scroll back to to if is not under touch
     */
    private void tryScrollBackToTop() {
        if (!mIndicator.isUnderTouch()) {
            mScrollChecker.tryToScrollTo(mIndicator.POS_START, mDurationToCloseHeader);
        }
    }

    /**
     * just make easier to understand
     */
    private void tryScrollBackToTopWhileLoading() {
        tryScrollBackToTop();
    }

    class ScrollChecker implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;

        public ScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            if (deltaY != 0) {
                DLog.v("MoveMoment",
                        "scroll: %s, start: %s, to: %s, currentPos: %s, current :%s, last: %s, delta: %s",
                        finish, mStart, mTo, mIndicator.getCurrentPosY(), curY, mLastFlingY, deltaY);
            }
            if (!finish) {
                mLastFlingY = curY;
                movePos(deltaY);
                post(this);
            } else {
                finish();
            }
        }

        private void finish() {
            DLog.v(LOG_TAG, "finish, currentPos:%s", mIndicator.getCurrentPosY());
            reset();
//            onPtrScrollFinish();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        private void destroy() {
            reset();
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
//                onPtrScrollAbort();
                reset();
            }
        }

        public void tryToScrollTo(int to, int duration) {
            if (mIndicator.isAlreadyHere(to)) {
                return;
            }
            mStart = mIndicator.getCurrentPosY();
            mTo = to;
            int distance = to - mStart;
            DLog.d(LOG_TAG, "tryToScrollTo: start: %s, distance:%s, to:%s", mStart, distance, to);
            removeCallbacks(this);

            mLastFlingY = 0;

            // fix #47: Scroller should be reused, https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh/issues/47
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
            mIsRunning = true;
        }
    }

    public XrlHandler getXrlHandler() {
        return mXrlHandler;
    }

    public void setXrlHandler(XrlHandler mXrlHandler) {
        this.mXrlHandler = mXrlHandler;
    }
}
