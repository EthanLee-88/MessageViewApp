package com.example.messageviewapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 *
 */
public class MessageViewOnTouchListener implements View.OnTouchListener {

    private MessageView mMessageView;
    private WindowManager mWindowManager;
    private Context mContext;
    private WindowManager.LayoutParams mParams;
    // ACTION_DOWN 落点的位置
    private float downX, downY;

    public MessageViewOnTouchListener(Context context) {
        super();
        mContext = context;
        mMessageView = new MessageView(mContext);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        // 透明
        mParams.format = PixelFormat.TRANSPARENT;
        // 设置外部可点击
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // 添加 MessageView
                addView();
                // 获取绑定 View 的Bitmap
                setBitmap(v);
                // 记录 ACTION_DOWN 落点的位置，用以计算绘制图形的初始位置
                downX = event.getX();
                downY = event.getY();
                // 开始绘制
                mMessageView.pointReset(event.getRawX() - downX + v.getWidth() / 2,
                        event.getRawY() - StatusBarUtil.getStatusBarHeight(v.getContext()) - downY + v.getHeight() / 2);
                // 隐藏绑定的View
                v.setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                // 重绘
                mMessageView.updatePoint(event.getRawX() - downX + v.getWidth() / 2,
                        event.getRawY() - StatusBarUtil.getStatusBarHeight(v.getContext()) - downY + v.getHeight() / 2);
                break;
            case MotionEvent.ACTION_UP:

                mMessageView.setActionUp(v, event);
                break;
            default: break;
        }
        return true;
    }

    /**
     * 往Window添加自动逸View
     */
    public void addView(){
        if (mMessageView == null) return;
        mWindowManager.addView(mMessageView, mParams);
    }

    /**
     * 移除View释放焦点
     */
    public void removeView() {
        if (mWindowManager == null) return;
        mWindowManager.removeView(mMessageView);
    }

    public void release(){
        mContext = null;
    }

    private void setBitmap(View view){
        mMessageView.setDrawBitmap(getViewBitmap(view));
    }

    /**
     * 创建并获取View的Bitmap
     *
     * @param view view
     * @return
     */
    public Bitmap getViewBitmap(View view) {
        view.buildDrawingCache();
        return view.getDrawingCache();
    }
}
