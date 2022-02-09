package at.aau.moose_scroll.controller;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.INVALID_POINTER_ID;

import static at.aau.moose_scroll.data.Consts.TECHNIQUE.DRAG;
import static at.aau.moose_scroll.data.Consts.TECHNIQUE.FLICK;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import at.aau.moose_scroll.data.Consts.*;
import at.aau.moose_scroll.data.Memo;
import at.aau.moose_scroll.tools.Logs;

import static at.aau.moose_scroll.controller.Logger.*;

public class Actioner {
    private final String NAME = "Actioner/";
    // -------------------------------------------------------------------------------

    private static Actioner instance; // Singelton instance

    // Mode of scrolling
    private TECHNIQUE mActiveTechnique = DRAG;

    private final int PPI = 312; // For calculating movement in mm

    // Algorithm parameters
    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
    private int leftmostIndex = INVALID_POINTER_ID; // Index of the leftmost finger
    private int actionIndex = INVALID_POINTER_ID; // New finger's index
    private PointF lastPoint;
    private int nTouchPoints; // = touchPointCounter in Demi's code
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mNumMovePoints = 0;
    private PointF mLeftmostTouchPoint;
    private PointF mLastTouchPoint;
    private double[] mLastVelocities = new double[]{};
    private int mTotalDistanceX = 0;
    private int mTotalDistanceY = 0;
    private boolean mAutoscroll = false;
    private long mTimeLastMoved;
    private boolean mContinueScroll = false;
    private double THRSH_MM = 1.0; // Threshold to ignore less than

    // Config
    private int mDragSensitivity = 2; // Count every n ACTION_MOVEs
    private double mDragGain = 20; // Gain factor for drag
    private double mRBGain = 1.5; // Gain factor for rate-based
    private int mRBSensititivity = 1; // Count every n ACTION_MOVEs (rate-based)
    private int mRBDenom = 50; // Denominator in RB's speed formula
    private double mFlickCoef = 0.3; // dX, dY returned from webView * coef -> Desktop

    // Views
    private WebView mWebView;

    // -------------------------------------------------------------------------------

    /**
     * Get the Singleton instance
     * @return Actioner instance
     */
    public static Actioner get() {
        if (instance == null) instance = new Actioner();
        return instance;
    }

    /**
     * Set the config
     * @param memo Memo from Desktop
     */
    public void config(Memo memo) {
        final String TAG = NAME + "config";
        Logs.d(TAG, memo);
        switch (memo.getMode()) {
        case STRINGS.TECH: {
            mActiveTechnique = TECHNIQUE.get(memo.getValue1Int());
            break;
        }

        case STRINGS.SENSITIVITY: {
            if (mActiveTechnique.equals(TECHNIQUE.DRAG))
                mDragSensitivity = memo.getValue1Double();
            if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED))
                mRBSensititivity = memo.getValue1Double();

            break;
        }

        case STRINGS.GAIN: {
            if (mActiveTechnique.equals(TECHNIQUE.DRAG))
                mDragGain = memo.getValue1Double();
            if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED))
                mRBGain = memo.getValue1Double();

            break;
        }

        case STRINGS.DENOM: {
            mRBDenom = memo.getValue1Int();
            break;
        }

        case STRINGS.COEF: {
            mFlickCoef = memo.getValue1Double();
            break;
        }
        }
    }

    /**
     * Set the WebView
     * @param view View (got from MainAActivity)
     * @param pagePath Path to the html file
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setWebView(View view, String pagePath) {
        String TAG = NAME + "setWebView";

        mWebView = (WebView) view;
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.loadUrl(pagePath);
        mWebView.scrollTo(200000, 200000);

        // Scrolling listners
        mWebView.setOnTouchListener((v, event) -> false);
        mWebView.setOnScrollChangeListener(new flickWebViewScrollListener());
    }

    /**
     * Perform the action
     * @param event MotionEvent to process and perform
     */
    public void scroll(MotionEvent event) {
        String TAG = NAME + "scroll";

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final float x = event.getX(pointerIndex);
            final float y = event.getY(pointerIndex);
            mLastTouchPoint = new PointF(x, y);

            mActivePointerId = event.getPointerId(pointerIndex);
            mNumMovePoints = 1;

            // Flick ----------------------------------------------------
            if (mActiveTechnique.equals(FLICK)) {
                mContinueScroll = true;
                mWebView.dispatchTouchEvent(event);

                // Log the event
                Logger.get().logMotionEventInfo(new MotionEventInfo(event));
            }

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            final int activeIndex = event.findPointerIndex(mActivePointerId);

            // Same finger is returned
            if (pointerId == mActivePointerId) {
                mNumMovePoints = 1;
                final float x = event.getX(activeIndex);
                final float y = event.getY(activeIndex);
                mLastTouchPoint = new PointF(x, y);

                // Flick ----------------------------------------------------
                if (mActiveTechnique.equals(FLICK)) {
                    mContinueScroll = true;

                    final MotionEvent newEvent = getNewEvent(event);
                    mWebView.dispatchTouchEvent(newEvent);

                    // Log the event
                    Logger.get().logMotionEventInfo(new MotionEventInfo(event));
                }

            } else { // New pointer
                // If the new pointer is added to the left
                if (activeIndex != -1 && event.getX(pointerIndex) < event.getX(activeIndex)) {
                    mNumMovePoints = 1;
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    mLastTouchPoint = new PointF(x, y);

                    mActivePointerId = event.getPointerId(pointerIndex);

                    // Flick ----------------------------------------------------
                    if (mActiveTechnique.equals(FLICK)) {
                        mContinueScroll = true;

                        final MotionEvent newEvent = getNewEvent(event);
                        mWebView.dispatchTouchEvent(newEvent);

                        // Log the event
                        Logger.get().logMotionEventInfo(new MotionEventInfo(event));
                    }
                }
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            Logs.d(TAG, mActivePointerId);
            Logs.d(TAG, mContinueScroll);

            final int activeIndex = event.findPointerIndex(mActivePointerId);
            if (activeIndex == -1) break;

            final float x = event.getX(activeIndex);
            final float y = event.getY(activeIndex);

            // DRAG ----------------------------------------------------
            if (mActiveTechnique.equals(TECHNIQUE.DRAG)) {

                if (mNumMovePoints % mDragSensitivity == 0) {
                    final double dX = x - mLastTouchPoint.x;
                    final double dY = y - mLastTouchPoint.y;

                    double vtScrollMM = px2mm(dY * mDragGain);
                    double hzScrollMM = px2mm(dX * mDragGain);

                    Logs.d(TAG, "DRAG vt|hz", vtScrollMM, hzScrollMM);
                    if (Math.abs(vtScrollMM) > THRSH_MM || Math.abs(hzScrollMM) > THRSH_MM) {
                        Memo memo = new Memo(STRINGS.SCROLL,
                                mActiveTechnique.toString(),
                                vtScrollMM, hzScrollMM);
                        Networker.get().sendMemo(memo);
                    }

                    // Update the last point
                    mLastTouchPoint = new PointF(x, y);
                }

            }
            //-----------------------------------------------------------

            // RATE-BASED -----------------------------------------------
            if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED)) {

                if (mNumMovePoints % mRBSensititivity == 0) {
                    final double dX = x - mLastTouchPoint.x;
                    final double dY = y - mLastTouchPoint.y;

                    final double dXAbsMM = px2mm(Math.abs(dX));
                    final double dXMM = Math.pow(dXAbsMM, mRBGain) / mRBDenom; // My version
                    final int dirDX = (int) (dX / Math.abs(dX));

                    final double dYAbsMM = px2mm(Math.abs(dY));
                    final double dYMM = Math.pow(dYAbsMM, mRBGain) / mRBDenom; // My version
                    final int dirDY = (int) (dY / Math.abs(dY));

                    final double vtScrollMM = dYMM * dirDY;
                    final double hzScrollMM = dXMM * dirDX;

                    Logs.d(TAG, "RATE-BASED", vtScrollMM, hzScrollMM);
                    // Only send amounts > 1 mm ( > ~ 1.2 px)
                    if (dYAbsMM > THRSH_MM || dXAbsMM > THRSH_MM) {
                        Memo memo = new Memo(STRINGS.SCROLL,
                                mActiveTechnique.toString(),
                                vtScrollMM, hzScrollMM);
                        Networker.get().sendMemo(memo);
                    }

                    // Last point shouldn't change!
                }

            }
            //-----------------------------------------------------------

            // FLICK ----------------------------------------------------
            if (mActiveTechnique.equals(FLICK)) {
                if (mContinueScroll) {
                    final MotionEvent newEvent = getNewEvent(event);
                    Logs.d(TAG, newEvent);
                    mWebView.dispatchTouchEvent(newEvent);

                    // Log the event
                    Logger.get().logMotionEventInfo(new MotionEventInfo(event));
                }

            }
            //-----------------------------------------------------------

            mNumMovePoints++;

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);

            // If the left finger left the screen, find the next leftmost
            // IMPORTANT: the left finger still counts in "getPointerCount()"
            if (pointerId == mActivePointerId) {
//                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//
//                final float x = event.getX(newPointerIndex);
//                final float y = event.getY(newPointerIndex);
//                mLastTouchPoint = new PointF(x, y);
//
//                mActivePointerId = event.getPointerId(newPointerIndex);

                // RATE-BASED ----------------------------------------------------------
                if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED)) stopScroll();

                // FLICK ---------------------------------------------------------------
                if (mActiveTechnique.equals(FLICK)) {
                    final MotionEvent newEvent = getNewEvent(event);
                    mWebView.dispatchTouchEvent(newEvent);

                    mContinueScroll = false;

                    // Log the event
                    Logger.get().logMotionEventInfo(new MotionEventInfo(event));
                }

            }

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            // RATE-BASED ----------------------------------------------------------
            if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED)) stopScroll();
            // FLICK ---------------------------------------------------------------
            if (mActiveTechnique.equals(FLICK)) {
                mWebView.dispatchTouchEvent(event);
                mContinueScroll = false;

                // Log the event
                Logger.get().logMotionEventInfo(new MotionEventInfo(event));
            }

            break;
        }

        }
    }

    /******
     * Class for managing scroll in webView
     */
    private class flickWebViewScrollListener implements View.OnScrollChangeListener {
        final String TAG = NAME + "flickWebViewScrollListener";

        @Override
        public void onScrollChange(View v,
                                   int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (oldScrollY != scrollY) Log.d(TAG, "Y= " + oldScrollY + " -> " + scrollY);
            if (oldScrollX != scrollX) Log.d(TAG, "X= " + oldScrollX + " -> " + scrollX);
            final double dY = (scrollY - oldScrollY) * mFlickCoef * (-1);
            final double dX = (scrollX - oldScrollX) * mFlickCoef * (-1);

            Networker.get().sendMemo(new Memo(STRINGS.SCROLL, FLICK, dY, dX));
        }
    }


    /**
     * Get a new MotionEvent to send to webView for Flick
     * @param oldEvent Old event to base
     * @return New MotionEvent
     */
    private MotionEvent getNewEvent(MotionEvent oldEvent) {
        final int newPointerCount = 1;
        if (mActivePointerId != INVALID_POINTER_ID) {
            final int activeIndex = oldEvent.findPointerIndex(mActivePointerId);
            int newAction = ACTION_DOWN;

            switch (oldEvent.getActionMasked()) {
            case ACTION_POINTER_DOWN: newAction = ACTION_DOWN;
                break;
            case ACTION_POINTER_UP: newAction = ACTION_UP;
                break;
            case ACTION_MOVE: newAction = ACTION_MOVE;
                break;
            }

            final MotionEvent.PointerProperties[] newProps =
                    new MotionEvent.PointerProperties[newPointerCount];
            newProps[0] = new MotionEvent.PointerProperties();
            oldEvent.getPointerProperties(activeIndex, newProps[0]);

            final MotionEvent.PointerCoords[] newCoords =
                    new MotionEvent.PointerCoords[newPointerCount];
            newCoords[0] = new MotionEvent.PointerCoords();
            oldEvent.getPointerCoords(activeIndex, newCoords[0]);

            final MotionEvent newEvent = MotionEvent.obtain(
                    oldEvent.getDownTime(), oldEvent.getEventTime(),
                    newAction, newPointerCount,
                    newProps, newCoords,
                    oldEvent.getMetaState(), oldEvent.getButtonState(),
                    oldEvent.getXPrecision(), oldEvent.getYPrecision(),
                    oldEvent.getDeviceId(), oldEvent.getEdgeFlags(),
                    oldEvent.getSource(), oldEvent.getFlags()
            );

            return newEvent;
        } else {
            return MotionEvent.obtain(oldEvent);
        }
    }

    private void resetFlick() {
        mLastVelocities = new double[]{0.0, 0.0, 0.0};
        mTotalDistanceX = 0;
        mTotalDistanceY = 0;
        mTimeLastMoved = System.currentTimeMillis();
        if (mAutoscroll) {
            stopScroll();
            mAutoscroll = false;
        }
    }

    /**
     * Stop any scrolling (no matter the technique)
     */
    private void stopScroll() {
        Networker.get().sendMemo(Memo.RB_STOP_MEMO);
    }

    /**
     * Check if a pointer is leftmost
     * @param me MortionEvent
     * @param pointerIndex index of the pointer to check
     * @return boolean
     */
    public boolean isLeftMost(MotionEvent me, int pointerIndex) {
        return findLeftMostIndex(me) == pointerIndex;
    }

    /**
     * Find the index of leftmost pointer
     * @param me MotionEvent
     * @return Index of the leftmost pointer
     */
    public int findLeftMostIndex(MotionEvent me) {
        String TAG = NAME + "findLeftMostIndex";

        int nPointers = me.getPointerCount();
        Logs.d(TAG, "nPointers", me.getPointerCount());
        if (nPointers == 0) return -1;
        if (nPointers == 1) return 0;

        // > 1 pointers (POINTER_DOWN or POINTER_UP)
        int lmIndex = 0;
        for (int pix = 0; pix < me.getPointerCount(); pix++) {
            if (me.getX(pix) < me.getX(lmIndex)) lmIndex = pix;
        }

        return lmIndex;
    }

    /**
     * Find the id of the leftmost pointer
     * @param me MotionEvent
     * @return Id of the leftmost pointer
     */
    private int findLeftMostId(MotionEvent me) {
        int lmIndex = findLeftMostIndex(me);
        if (lmIndex == -1) return INVALID_POINTER_ID;
        else return me.getPointerId(lmIndex);
    }

    /**
     * Update the leftmost properties and lastPoint
     */
    private void updatePointers(MotionEvent me) {
        String TAG = NAME + "updatePointers";

        leftmostIndex = findLeftMostIndex(me);
        leftmostId = me.getPointerId(leftmostIndex);
        lastPoint = new PointF(me.getX(leftmostIndex), me.getY(leftmostIndex));

        Logs.d(TAG, "ind|id|point", leftmostIndex, leftmostId, lastPoint.x);
    }

    /**
     * Truly GET the PointerCoords!
     * @param me MotionEvent
     * @param pointerIndex Pointer index
     * @return PointerCoords
     */
    public MotionEvent.PointerCoords getPointerCoords(MotionEvent me, int pointerIndex) {
        MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
        me.getPointerCoords(pointerIndex, result);
        return result;
    }

    private double px2mm(double px) {
        return (px / PPI) * 25.4;
    }

}
