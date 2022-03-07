package at.aau.proto_moose.controller;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.INVALID_POINTER_ID;

import static at.aau.proto_moose.data.Consts.TECHNIQUE.FLICK;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import at.aau.proto_moose.data.Consts.*;
import at.aau.proto_moose.data.Memo;
import at.aau.proto_moose.tools.Logs;

import static at.aau.proto_moose.controller.Logger.*;

public class Actioner {
    private final String NAME = "Actioner/";

    private static Actioner instance; // Singelton instance

    // Constants
    private final int PPI = 312; // For calculating movement in mm

    // Algorithm parameters
    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
    private int leftmostIndex = INVALID_POINTER_ID; // Index of the leftmost finger
//    private int actionIndex = INVALID_POINTER_ID; // New finger's index
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
     * Set the config (got Memo from Netwroker)
     * @param memo Memo from Desktop
     */
    public void config(Memo memo) {
        final String TAG = NAME + "config";
        Logs.d(TAG, memo);
    }


    /**
     * Process the event and create the action (to send to dekstop)
     * @param event MotionEvent to process and perform
     */
    public void act(MotionEvent event) {
        String TAG = NAME + "scroll";

        switch (event.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = event.getActionIndex();
            mActivePointerId = event.getPointerId(pointerIndex);
            mNumMovePoints = 1;

            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);
            final int activeIndex = event.findPointerIndex(mActivePointerId);

            // Same finger is returned
            if (pointerId == mActivePointerId) {
                mNumMovePoints = 1;

            } else { // New pointer
                // If the new pointer is added to the left
                if (activeIndex != -1 && event.getX(pointerIndex) < event.getX(activeIndex)) {
                    mNumMovePoints = 1;
                    mActivePointerId = event.getPointerId(pointerIndex);
                }
            }

            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int activeIndex = event.findPointerIndex(mActivePointerId);
            if (activeIndex == -1) break;

            mNumMovePoints++;

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = event.getActionIndex();
            final int pointerId = event.getPointerId(pointerIndex);

            // If the left finger left the screen, find the next leftmost
            // IMPORTANT: the left finger still counts in "getPointerCount()"
//            if (pointerId == mActivePointerId) {
//                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//
//                final float x = event.getX(newPointerIndex);
//                final float y = event.getY(newPointerIndex);
//                mLastTouchPoint = new PointF(x, y);
//
//                mActivePointerId = event.getPointerId(newPointerIndex);
//
//            }

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;

            break;
        }

        }
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

    /**
     * Pixels to millimeters
     * @param px
     * @return
     */
    private double px2mm(double px) {
        return (px / PPI) * 25.4;
    }

}
