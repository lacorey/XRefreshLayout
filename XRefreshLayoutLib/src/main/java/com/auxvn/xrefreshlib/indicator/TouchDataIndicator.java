package com.auxvn.xrefreshlib.indicator;

import android.graphics.PointF;

/**
 * Created by zhaoxin on 16/9/24.
 */

public class TouchDataIndicator {
    private PointF mTouchDown = new PointF(0,0);
    private PointF mMovePoint = new PointF(0,0);


    public PointF getTouchDown(){
        return mTouchDown;
    }

    public void setMovePoint(float x,float y){
        mMovePoint.x += x;
        mMovePoint.y += y;
    }

    public PointF getMovePoint(){
        return mMovePoint;
    }

    public void clearMovePoint(){
        mMovePoint.x = 0;
        mMovePoint.y = 0;
    }
}
