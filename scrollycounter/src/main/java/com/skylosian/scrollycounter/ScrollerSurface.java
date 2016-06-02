package com.skylosian.scrollycounter;

/**
 * Created by dihnen on 4/23/16.
 */

import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

public class ScrollerSurface extends SurfaceView {


    private static Bitmap bmap;
    private final GestureDetectorCompat mGestureDetector;
    private float present_position = 0F;
    private float actionPosition = 0F;

    private ScrollPosition position;
    private float originPosition;
    private OverScroller mScroller;
    private Bundle state;
    private TimeAnimator animator;
    private float lastVelocity = 0;

    public ScrollerSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        context.getTheme();

        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mScroller = new OverScroller(context, new AccelerateDecelerateInterpolator(), 1F, 1F, true);

        position = new ScrollPosition(0L);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = new TimeAnimator() {
        };
        animator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animator, long totalTime, long deltaTime) {
                mScroller.computeScrollOffset();
                float delta = lastVelocity - mScroller.getCurrVelocity();
                if (!mScroller.isFinished()) Log.v("fraction", Float.valueOf(position.pageFraction()).toString());
                if (mScroller.getCurrY() != lastCurrY) {
                    ViewCompat.postInvalidateOnAnimation(ScrollerSurface.this);
                }
                if (mScroller.isFinished()) {
                    lastCurrY = mScroller.getCurrY();
                    return;
                }
                synchronized ("position") {
                    position = position.scroll(Long.valueOf((long)Math.floor(1000 * (Float.valueOf(mScroller.getCurrY() - lastCurrY) / Float.valueOf(getHeight())))));
                    lastCurrY = mScroller.getCurrY();
                }
            }
        });

        animator.start();
    }

    @Override
    public void onDetachedFromWindow() {
        animator.removeAllUpdateListeners();
        animator.cancel();
        animator = null;
        bmap = null;

        super.onDetachedFromWindow();
    }

    public void setPosition(ScrollPosition pos) {
        position = pos;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public ScrollPosition getPosition() {
        return position;
    }

    public ScrollingActivity getActivity() {
        return (ScrollingActivity) this.getContext();
    }

    @Override
    public void onDraw(Canvas canvas) {
        redraw(canvas);
    }

    @Override
    public void onMeasure(int height, int width) {
        Log.v("size", "measured " + height + " by " + width);
        super.onMeasure(height, width);
    }

    public List<Rect> rects(ScrollPosition pos) {
        List<Rect> list = new ArrayList<Rect>();
        int numbars = pos.nextPage().bitwidth();
        int width = Math.round(getWidth() / numbars);
        int offset = Math.round(pos.pageFraction() * getHeight());
        for (int i = 0; i < numbars; i++) {
            list.add(new Rect(offset - getHeight(), i * width, offset, (i + 1) * width));
        }
        return list;
    }

    public Bitmap getBitmap() {
        if (bmap == null) {
            bmap = Bitmap.createBitmap(getWidth(), getHeight() * 2, Bitmap.Config.ARGB_8888);
        }
        return bmap;
    }

    private Long lastPageDrawn = Long.valueOf(-1);
    public void redraw(Canvas canvasMaster) {
        Canvas canvas = new Canvas(getBitmap());
        int accent = getResources().getColor(R.color.colorAccent);
        int primary = getResources().getColor(R.color.colorPrimary);
        int textColor = getResources().getColor(R.color.abc_primary_text_material_dark);
        Shader gradientDownShader = new LinearGradient(1, 1, 1, getHeight(), primary, accent, Shader.TileMode.MIRROR);
        Shader gradientUpShader = new LinearGradient(1, 1, 1, getHeight(), accent, primary, Shader.TileMode.MIRROR);
        Shader topShader = new LinearGradient(1, 1, 1, getHeight(), accent, accent, Shader.TileMode.MIRROR);
        Shader botShader = new LinearGradient(1, 1, 1, getHeight(), primary, primary, Shader.TileMode.MIRROR);
        Shader blackShader = new LinearGradient(1, 1, 1, getHeight(), textColor, textColor, Shader.TileMode.MIRROR);
        Paint wetPaint = new Paint();
        wetPaint.setStyle(Paint.Style.FILL);
        wetPaint.setStrokeWidth(500);
        wetPaint.setTextSize(100F);

//        if (!lastPageDrawn.equals(position.pageNumber())) {
            canvas.drawColor(primary);

            int bars = position.prevBitwidth();
            int width = getWidth();
            if (bars > 0) {
                width = getWidth() / bars;
            }
            for (int i = 0; i < bars; i++) {

                float boty = getHeight();
                float topx = i * width + 1;
                float topy = 0;
                float botx = (i + 1) * width;
                wetPaint.setShader(ScrollPosition.shaderByChange(i, position.prevPage(), position, gradientUpShader, gradientDownShader, topShader, botShader));
                canvas.drawRect(topx, topy, botx, boty, wetPaint);
            }

            ScrollPosition nextPage = position.nextPage();
            int nbars = nextPage.bitwidth();
            int nwidth = nbars == 0 ? 1 : getWidth() / nbars;
            for (int i = 0; i < nbars; i++) {

                float topy = getHeight();
                float topx = i * nwidth + 1;
                float boty = getHeight() * 2;
                float botx = (i + 1) * nwidth;
                wetPaint.setShader(ScrollPosition.shaderByChange(i, position, position.nextPage(), gradientUpShader, gradientDownShader, topShader, botShader));
                canvas.drawRect(topx, topy, botx, boty, wetPaint);
            }
//       }
        lastPageDrawn = position.pageNumber();

        Rect tangle = new Rect(0, Math.round(getHeight() * position.pageFraction()), getWidth(), Math.round(getHeight() * position.pageFraction() + getHeight()));
        RectF screenRect = new RectF(0, 0, getWidth(), getHeight());
        canvasMaster.drawBitmap(getBitmap(), tangle, screenRect, wetPaint);
        wetPaint.setShader(blackShader);
        canvasMaster.drawText(position.getPosition().toString(), 100F, 100F, wetPaint);
        canvasMaster.drawText(position.prevPage().pageNumber().toString(), 100F, 200F, wetPaint);
        canvasMaster.drawText(position.pageNumber().toString(), 100F, 300F, wetPaint);
        canvasMaster.drawText(position.nextPage().pageNumber().toString(), 100F, 400F, wetPaint);
        canvasMaster.drawText(Math.floor(position.pageFraction()*100) + " ", 100F, 500F, wetPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        ViewCompat.postInvalidateOnAnimation(this);
        super.onTouchEvent(event);
        return true;
    }

    int lastCurrY = 0;
    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls,
     * and flings.
     */
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            synchronized ("position") {
                ScrollerSurface.this.getActivity().setPosition(position = position.scroll(distanceY / getHeight() * 1000));
            }
            mScroller.abortAnimation();
            ViewCompat.postInvalidateOnAnimation(ScrollerSurface.this);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling(0, Math.round(getHeight() * position.pageFraction()), 0, Math.round(-velocityY), 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            lastCurrY = mScroller.getCurrY();
            Log.v("fling", "FLING" + mScroller.getCurrVelocity());

            //           position = position.scroll(1000*(-1*velocityY/getHeight()));
            ViewCompat.postInvalidateOnAnimation(ScrollerSurface.this);
            return true;
        }
    };

}

