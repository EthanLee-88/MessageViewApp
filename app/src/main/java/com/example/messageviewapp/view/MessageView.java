package com.example.messageviewapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.messageviewapp.R;

import java.util.ArrayList;

public class MessageView extends View {
    private Paint circlePaint;
    // 定点圆和拖拽点圆的圆心
    private PointF movePoint, underPoint;
    // 拖拽圆半径
    private final double mRadius = 20;
    private double distanceCount = 1;
    // 绑定控件的事件监听
    private static MessageViewOnTouchListener mMessageViewOnTouchListener;
    // 绑定控件的Bitmap
    private static Bitmap drawBitmap;
    // 爆炸粉碎效果切图
    private ArrayList<Integer> bombBitmapIds = new ArrayList<>();
    private int currentIndex = 0;

    public MessageView(Context context) {
        this(context, null);
    }

    public MessageView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRes(context, attrs, defStyleAttr);
    }

    private void initRes(Context context, AttributeSet attrs, int defStyleAttr) {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setDither(true);
        circlePaint.setColor(context.getResources().getColor(R.color.design_default_color_primary_dark));
        movePoint = new PointF();
        underPoint = new PointF();
        bombBitmapIds.add(R.mipmap.explode_1);
        bombBitmapIds.add(R.mipmap.explode_2);
        bombBitmapIds.add(R.mipmap.explode_3);
        bombBitmapIds.add(R.mipmap.explode_4);
        bombBitmapIds.add(R.mipmap.explode_5);
    }

    private double getPointDistance() {
        // 求两点的距离
        return Math.sqrt(Math.abs(movePoint.x - underPoint.x) * Math.abs(movePoint.x - underPoint.x) +
                Math.abs(movePoint.y - underPoint.y) * Math.abs(movePoint.y - underPoint.y));
    }

    /**
     * 计算两圆心距离与拖动圆直径之比
     */
    private void countDistance() {
        if (getPointDistance() > 2 * mRadius) {
            distanceCount = getPointDistance() / (2 * mRadius);
        }
    }

    /**
     * 生成贝塞尔曲线
     *
     * @return
     */
    private Path getBezPath() {
        Path bezPath = new Path();
        float underRadiusPx = dip2px((float) (mRadius / distanceCount));
        float moveRadiusPx = dip2px((float) mRadius);

        // 计算 delta X 、delta Y 及 ∠a 的值 arcTanA
        float Dx = movePoint.x - underPoint.x;
        float Dy = movePoint.y - underPoint.y;
        double arcTanA = Math.atan(Dy / Dx);

        // 计算 p0 及 p3 坐标
        float dx0 = (float) (underRadiusPx * Math.sin(arcTanA));
        float dy0 = (float) (underRadiusPx * Math.cos(arcTanA));
        float x0 = underPoint.x + dx0;
        float y0 = underPoint.y - dy0;
        float x3 = underPoint.x - dx0;
        float y3 = underPoint.y + dy0;

        // 计算 p1 及 p2 坐标
        float dx1 = (float) (moveRadiusPx * Math.sin(arcTanA));
        float dy1 = (float) (moveRadiusPx * Math.cos(arcTanA));
        float x1 = movePoint.x + dx1;
        float y1 = movePoint.y - dy1;
        float x2 = movePoint.x - dx1;
        float y2 = movePoint.y + dy1;

        // 画出包含两条贝塞尔曲线的闭合曲线
        bezPath.moveTo(x0, y0);
        float controlX = (underPoint.x + movePoint.x) / 2;
        float controlY = (underPoint.y + movePoint.y) / 2;
        bezPath.quadTo(controlX, controlY, x1, y1);
        bezPath.lineTo(x2, y2);
        bezPath.quadTo((underPoint.x + movePoint.x) / 2, (underPoint.y + movePoint.y) / 2, x3, y3);
        bezPath.close();
        return bezPath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawCircle(movePoint.x, movePoint.y, dip2px((float) mRadius), circlePaint);
        if (distanceCount < 10) {
            // 大于一定距离后原点消失
            canvas.drawCircle(underPoint.x, underPoint.y, dip2px((float) (mRadius / distanceCount)), circlePaint);
            // 获取并绘制贝塞尔曲线
            Path bezPath = getBezPath();
            canvas.drawPath(bezPath, circlePaint);
        }
        // 绘制被绑定拖拽控件的 Bitmap
        canvas.drawBitmap(drawBitmap, movePoint.x - drawBitmap.getWidth() / 2,
                movePoint.y - drawBitmap.getHeight() / 2, null);
    }

    public void pointReset(float x, float y) {
        movePoint.set(x, y);
        underPoint.set(x, y);
        invalidate();
    }

    /**
     * 更新手指移动点
     *
     * @param x
     * @param y
     */
    public void updatePoint(float x, float y) {
        if (movePoint != null) {
            movePoint.x = x;
            movePoint.y = y;
            countDistance();
            invalidate();
        }
    }

    public void setActionUp(View v, MotionEvent event) {
        if (distanceCount < 10) {
            // 重新显示原来的View
            v.setVisibility(View.VISIBLE);
            // 从WindowManager 移除 MessageView，释放焦点
            mMessageViewOnTouchListener.removeView();
        } else { // View 粉碎
            showBomb(bombBitmapIds.get(0), v);
        }
    }

    /**
     * 实现爆炸的动画效果
     *
     * @param id
     * @param v
     */
    private void showBomb(int id, View v) {
        if (id == R.mipmap.explode_5) {
            currentIndex = 0;
            // 从WindowManager 移除 MessageView，释放焦点
            mMessageViewOnTouchListener.removeView();
            // 接口通知外界
            setDismiss(v);
            return;
        }
        postDelayed(() -> {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
            setDrawBitmap(bitmap);
            invalidate();
            currentIndex += 1;
            showBomb(bombBitmapIds.get(currentIndex), v);
        }, 100);
    }

    private float dip2px(float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    // 设置拖动控件的Bitmap
    public void setDrawBitmap(Bitmap viewBitmap) {
        drawBitmap = viewBitmap;
    }

    /**
     * 绑定拖拽控件
     *
     * @param view
     */
    public static void bindView(View view) {
        if (view == null) return;
        if (mMessageViewOnTouchListener == null)
            mMessageViewOnTouchListener = new MessageViewOnTouchListener(view.getContext());
        view.setOnTouchListener(mMessageViewOnTouchListener);
    }

    /**
     * 释放 Context
     */
    public static void release() {
        if (drawBitmap != null) {
            drawBitmap.recycle();
            drawBitmap = null;
        }
        if (mMessageViewOnTouchListener == null) return;
        mMessageViewOnTouchListener.release();
        mMessageViewOnTouchListener = null;
    }

    public interface ViewDismissListener {
        void viewDismiss(View view);
    }

    private static ViewDismissListener mViewDismissListener;

    public static void setViewDismissListener(ViewDismissListener viewDismissListener) {
        mViewDismissListener = viewDismissListener;
    }

    private void setDismiss(View view) {
        if (mViewDismissListener != null) mViewDismissListener.viewDismiss(view);
    }
}
