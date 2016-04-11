package com.example.foo.snakegame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    protected int NUM_WIDE_BLOCKS = 40;
    protected GrassView mGrassView;
    protected Canvas mCanvas;
    protected Paint mPaint;
    protected int mScore;
    protected int mDisplayWidth;
    protected int mDisplayHeight;
    protected int mBlockSize;
    protected int mNumHighBlocks;
    protected int[] mSnakeXs;
    protected int[] mSnakeYs;
    protected int mAppleX;
    protected int mAppleY;
    protected int mSnakeSize = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mGrassView = new GrassView(this);
        this.setContentView(mGrassView);

        this.updateDisplayInfo();

        mSnakeXs = new int[999];
        mSnakeYs = new int[999];

        this.setSnake();
        this.setApple();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGrassView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGrassView.resume();
    }

    protected void updateDisplayInfo() {
        Point point = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(point);
        mDisplayHeight = point.y;
        mDisplayWidth = point.x;

        mBlockSize = mDisplayWidth / NUM_WIDE_BLOCKS;
        mNumHighBlocks = mDisplayHeight / mBlockSize;
        String msg = String.format("block size: %d, num high blocks: %d", mBlockSize, mNumHighBlocks);
//        this.d(msg);
    }

    protected void setSnake() {
        mSnakeXs[0] = (NUM_WIDE_BLOCKS / 2);
        mSnakeYs[0] = (mNumHighBlocks / 2);

        mSnakeXs[1] = (mSnakeXs[0] - 1);
        mSnakeYs[1] = mSnakeYs[0];

        mSnakeXs[2] = (mSnakeXs[1] - 1);
        mSnakeYs[2] = mSnakeYs[1];

        mSnakeXs[3] = (mSnakeXs[2] - 1);
        mSnakeYs[3] = mSnakeYs[2];
    }

    protected void setApple() {
        mAppleX = new Random().nextInt(NUM_WIDE_BLOCKS);
        mAppleY = new Random().nextInt(mNumHighBlocks);
        String s = String.format("apple x: %d, apple y: %d", mAppleX, mAppleY);
        d(s);
    }

    protected static void d(String msg) {
        Log.d(TAG, msg);
    }

    private class GrassView extends SurfaceView implements Runnable {

        protected Context mContext;
        protected SurfaceHolder mHolder;
        protected Thread mThrd;
        protected volatile boolean mIsDone;
        protected float mDownX, mDownY;
        protected boolean mMoveUp, mMoveRight, mMoveDown, mMoveLeft;

        public GrassView(Context context) {
            super(context);
            mContext = context;
            mHolder = this.getHolder();
            mPaint = new Paint();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    mDownY = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mMoveRight = mMoveLeft = mMoveUp = mMoveDown = false;
                    String s = String.format("downX: %f, donwY: %f, currX: %f, currY: %f",
                            mDownX, mDownY, event.getX(), event.getY());
//                    d(s);

                    float dX = event.getX() - mDownX,
                            dY = event.getY() - mDownY;
                    if (Math.abs(dX) > Math.abs(dY)) {
                        if (dX > 0) {
                            mMoveRight = true;
                        } else {
                            mMoveLeft = true;
                        }
                    } else {
                        if (dY > 0) {
                            mMoveDown = true;
                        } else {
                            mMoveUp = true;
                        }
                    }

//                    if (mMoveRight) d("right");
//                    if (mMoveLeft) d("left");
//                    if (mMoveUp) d("up");
//                    if (mMoveDown) d("down");

                    return true;
            }
            return super.onTouchEvent(event);
        }


        @Override
        public void run() {
            while (!this.mIsDone) {
                this.updateView();
                this.drawView();
                this.controlFPS();
            }
        }

        protected void updateView() {
            if (!mMoveRight && !mMoveDown && !mMoveLeft && !mMoveUp) return;

            // move body & tail
            for (int i = (mSnakeSize - 1); i > 0; i--) {
                mSnakeXs[i] = mSnakeXs[i - 1];
                mSnakeYs[i] = mSnakeYs[i - 1];
            }

            // move head..
            if (mMoveRight) {
                mSnakeXs[0]++;
            } else if (mMoveDown) {
                mSnakeYs[0]++;
            } else if (mMoveLeft) {
                mSnakeXs[0]--;
            } else if (mMoveUp) {
                mSnakeYs[0]--;
            }

            // detect collision detection with apple..
            if ((mSnakeXs[0] == mAppleX) && (mSnakeYs[0] == mAppleY)) {
                mScore += mSnakeSize;
                setApple();
                mSnakeSize++;
            }

            // check if snake head is touched the screen boundaries..
            if ((mSnakeXs[0] <= 0) || (mSnakeXs[0] >= (NUM_WIDE_BLOCKS - 1)) ||
                    (mSnakeYs[0] <= 0) || (mSnakeYs[0] >= (mNumHighBlocks - 1))) {
                mIsDone = true;
                ((Activity) this.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "The snake is dead!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        protected void drawView() {
            if (mHolder.getSurface().isValid()) {
                mCanvas = mHolder.lockCanvas();
                mCanvas.drawColor(Color.BLACK);
                mPaint.setColor(Color.YELLOW);

                // set head position and draw it..
                int left = (mSnakeXs[0] * mBlockSize),
                        top = (mSnakeYs[0] * mBlockSize),
                        right = (left + mBlockSize),
                        bottom = (top + mBlockSize);
                String msg = String.format("left: %d, top: %d, right: %d, bottom: %d", left, top, right, bottom);
//                d(msg);
                mCanvas.drawRect(left, top, right, bottom, mPaint);

                // set body & tail positions and draw it..
                for (int i = (mSnakeSize - 1); i > 0; i--) {
                    left = (mSnakeXs[i] * mBlockSize);
                    top = (mSnakeYs[i] * mBlockSize);
                    right = (left + mBlockSize);
                    bottom = (top + mBlockSize);
                    mPaint.setColor(Color.GREEN);
                    msg = String.format("left: %d, top: %d, right: %d, bottom: %d", left, top, right, bottom);
//                    d(msg);
                    mCanvas.drawRect(left, top, right, bottom, mPaint);
                }

                // draw the apple..
                mPaint.setColor(Color.RED);
                left = (mAppleX * mBlockSize);
                top = (mAppleY * mBlockSize);
                right = (left + mBlockSize);
                bottom = (top + mBlockSize);
                mCanvas.drawRect(left, top, right, bottom, mPaint);

                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

        protected void controlFPS() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }

        public void pause() {
            mIsDone = false;
        }

        public void resume() {
            mThrd = new Thread(this);
            mIsDone = false;
            mThrd.start();
        }
    }
}
