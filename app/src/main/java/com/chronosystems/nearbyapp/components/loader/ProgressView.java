/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chronosystems.nearbyapp.components.loader;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.chronosystems.nearbyapp.utils.AppConstants;

/**
 * Use {@link #setRefreshing(boolean)} to show/hide this view
 */
public class ProgressView extends CircleImageView {

    private static final int MAX_ALPHA = 255;
    private static final int CIRCLE_DIAMETER = 40;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private boolean mRefreshing = false;
    private int mMediumAnimationDuration;
    private boolean mScale;

    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    protected int mFrom;

    protected int mOriginalOffsetTop;

    private MaterialProgressDrawable mProgress;

    private Animation mScaleAnimation;

    private Animation mScaleDownAnimation;

    private AnimationListener mRefreshListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                // Make sure the progress view is fully visible
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
            } else {
                mProgress.stop();
                setVisibility(View.GONE);
                setColorViewAlpha(MAX_ALPHA);
                // Return the circle to its start position
                if (mScale) {
                    setAnimationProgress(0 /* animation complete and view is hidden */);
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop, true /* requires update */);
                }
            }
        }
    };

    private void setColorViewAlpha(int targetAlpha) {
        getBackground().setAlpha(targetAlpha);
        mProgress.setAlpha(targetAlpha);
    }

    /**
     * One of DEFAULT, or LARGE.
     */
    public void setSize(int size) {
        if (size != MaterialProgressDrawable.LARGE && size != MaterialProgressDrawable.DEFAULT) {
            return;
        }
        setImageDrawable(null);
        mProgress.updateSizes(size);
        setImageDrawable(mProgress);
    }

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public ProgressView(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        setImageDrawable(mProgress);
        setColorSchemeResources(AppConstants.LOADER.DEFAULT_COLORS);
        setVisibility(View.GONE);

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }


    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing && !mRefreshing) {
            // scale and show
            mRefreshing = refreshing;
            setTargetOffsetTopAndBottom(0, true /* requires update */);
            startScaleUpAnimation(mRefreshListener);
        } else {
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(0, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        bringToFront();
        offsetTopAndBottom(offset);
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private void startScaleUpAnimation(AnimationListener listener) {
        setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            // Pre API 11, alpha is used in place of scale up to show the
            // progress circle appearing.
            // Don't adjust the alpha during appearance otherwise.
            mProgress.setAlpha(MAX_ALPHA);
        }
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            setAnimationListener(listener);
        }
        clearAnimation();
        startAnimation(mScaleAnimation);
    }

    /**
     * Pre API 11, this does an alpha animation.
     *
     * @param progress
     */
    private void setAnimationProgress(float progress) {
        if (isAlphaUsedForScale()) {
            setColorViewAlpha((int) (progress * MAX_ALPHA));
        } else {
            ViewCompat.setScaleX(this, progress);
            ViewCompat.setScaleY(this, progress);
        }
    }


    private void startScaleDownAnimation(AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        setAnimationListener(listener);
        clearAnimation();
        startAnimation(mScaleDownAnimation);
    }

    /**
     * Set the color resources used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colorResIds
     */
    public void setColorSchemeResources(int... colorResIds) {
        final Resources res = getResources();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    public void setColorSchemeColors(int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            setAnimationListener(listener);
        }
        clearAnimation();
        startAnimation(mAnimateToCorrectPosition);
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = 0;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
        }
    };
}