package net.steamcrafted.loadtoast;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Wannes2 on 23/04/2015.
 */
public class LoadToast {
    private static final int HIDE = 769;
    private static final int SUCCESS = 712;
    private static final int ERROR = 242;

    private String mText = "";
    private LoadToastView mView;
    private ViewGroup mParentView;
    private int mTranslationY = 0;
    private boolean mShowCalled = false;
    private boolean mToastCanceled = false;
    private boolean mInflated = false;
    private boolean mVisible = false;
    private Handler mHandler;

    public LoadToast(Context context) {
        mView = new LoadToastView(context);
        mParentView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mView.setAlpha(0f);
        mParentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mView.setTranslationX((mParentView.getWidth() - mView.getWidth()) / 2);
                mView.setTranslationY(-mView.getHeight() + mTranslationY);
                mInflated = true;
                if (!mToastCanceled && mShowCalled) show();
            }
        }, 1);

        mParentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                checkZPosition();
            }
        });

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case SUCCESS:
                        mView.success();
                        slideUp();
                        break;
                    case ERROR:
                        mView.error();
                        slideUp();
                        break;
                    case HIDE:
                    default:
                        slideUp();
                        break;
                }
            }
        };
    }

    public LoadToast setTranslationY(int pixels) {
        mTranslationY = pixels;
        return this;
    }

    public LoadToast setText(String message) {
        mText = message;
        mView.setText(mText);
        return this;
    }

    public LoadToast setTextColor(int color) {
        mView.setTextColor(color);
        return this;
    }

    public LoadToast setBackgroundColor(int color) {
        mView.setBackgroundColor(color);
        return this;
    }

    public LoadToast setProgressColor(int color) {
        mView.setProgressColor(color);
        return this;
    }

    public LoadToast show() {
        if (!mInflated) {
            mShowCalled = true;
            return this;
        }
        mView.show();
        mView.setTranslationX((mParentView.getWidth() - mView.getWidth()) / 2);
        mView.setAlpha(0f);
        mView.setTranslationY(-mView.getHeight() + mTranslationY);
        //mView.setVisibility(View.VISIBLE);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.setStartDelay(0);
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(
                ObjectAnimator.ofFloat(mView, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(mView, View.TRANSLATION_Y, 25 + mTranslationY)
        );
        set.start();

        mVisible = true;
        checkZPosition();

        return this;
    }

    public void hide() {
        if (!mInflated) {
            mToastCanceled = true;
            return;
        }
        mHandler.obtainMessage(HIDE).sendToTarget();
    }

    public void success() {
        if (!mInflated) {
            mToastCanceled = true;
            return;
        }

        mHandler.obtainMessage(SUCCESS).sendToTarget();
    }

    public void error() {
        if (!mInflated) {
            mToastCanceled = true;
            return;
        }

        mHandler.obtainMessage(ERROR).sendToTarget();
    }

    private void checkZPosition() {
        // If the toast isn't visible, no point in updating all the views
        if (!mVisible) return;

        int pos = mParentView.indexOfChild(mView);
        int count = mParentView.getChildCount();
        if (pos != count - 1) {
            ((ViewGroup) mView.getParent()).removeView(mView);
            mParentView.requestLayout();
            mParentView.addView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void slideUp() {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.setStartDelay(1000);
        set.setInterpolator(new AccelerateInterpolator());
        set.playTogether(
                ObjectAnimator.ofFloat(mView, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(mView, View.TRANSLATION_Y, -mView.getHeight() + mTranslationY)
        );
        set.start();

        mVisible = false;
    }
}
