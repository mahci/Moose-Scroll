package at.aau.moose_scroll.controller;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.view.MotionEvent;

import at.aau.moose_scroll.data.Consts.*;
import at.aau.moose_scroll.data.Memo;
import at.aau.moose_scroll.tools.Logs;


public class Actioner {
    private final String NAME = "Actioner/";
    // -------------------------------------------------------------------------------

    private static Actioner instance; // Singelton instance

    // Mode of scrolling
    private TECHNIQUE mActiveTechnique = TECHNIQUE.DRAG;
//    private MODE mode = MODE.TWOD;

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

    private final int PPI = 312; // For calculating movement in mm

    private final Memo MEMO_STOP_RB = new Memo(
            STRINGS.SCROLL, STRINGS.RB,
            STRINGS.STOP, STRINGS.STOP);

    // Config
    private int mDragSensitivity = 2; // Count every n ACTION_MOVEs
    private double mDragGain = 100; // Gain factor for drag
    private double mRBGain = 1.5; // Gain factor for rate-based
    private int mRBSensititivity = 1; // Count every n ACTION_MOVEs (rate-based)
    private int mRBDenom = 50; // Denominator in RB's speed formula

    // -------------------------------------------------------------------------------

    /**
     * Get the Singleton instance
     * @return Actioner instance
     */
    public static Actioner get() {
        if (instance == null) instance = new Actioner();
        return instance;
    }

    public void config(Memo memo) {
        final String TAG = NAME + "config";

        switch (memo.getMode()) {
        case STRINGS.TECHNIQUE: {
            mActiveTechnique = TECHNIQUE.values()[memo.getValue1Int()];
            Logs.d(TAG, "New Technique", mActiveTechnique.toString());

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
        }

    }

    /**
     * Perform the action
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    public void scroll(MotionEvent mevent, int mid) {
        String TAG = NAME + "scroll";

        switch (mevent.getActionMasked()) {

        case MotionEvent.ACTION_DOWN: {
            final int pointerIndex = mevent.getActionIndex();
            final float x = mevent.getX(pointerIndex);
            final float y = mevent.getY(pointerIndex);
            mLastTouchPoint = new PointF(x, y);

            mActivePointerId = mevent.getPointerId(pointerIndex);
            mNumMovePoints = 1;


            break;
        }

        case MotionEvent.ACTION_POINTER_DOWN: {
            final int pointerIndex = mevent.getActionIndex();
            final int activeIndex = mevent.findPointerIndex(mActivePointerId);

            // If the new finger is to the left
            if (mevent.getX(pointerIndex) < mevent.getX(activeIndex)) {
                final float x = mevent.getX(pointerIndex);
                final float y = mevent.getY(pointerIndex);
                mLastTouchPoint = new PointF(x, y);

                mActivePointerId = mevent.getPointerId(pointerIndex);
                mNumMovePoints = 1;
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int activeIndex = mevent.findPointerIndex(mActivePointerId);
            final float x = mevent.getX(activeIndex);
            final float y = mevent.getY(activeIndex);

            // DRAG -------------------------------------------------
            if (mActiveTechnique.equals(TECHNIQUE.DRAG)) {

                if (mNumMovePoints % mDragSensitivity == 0) {
                    final double dX = x - mLastTouchPoint.x;
                    final double dY = y - mLastTouchPoint.y;

                    double vtScrollMM = px2mm(dY * mDragGain);
                    double hzScrollMM = px2mm(dX * mDragGain);

                    Logs.d(TAG, "DRAG vt|hz", vtScrollMM, hzScrollMM);
                    if (Math.abs(vtScrollMM) > 0.5 || Math.abs(hzScrollMM) > 0.5) {
                        Memo memo = new Memo(
                                STRINGS.SCROLL, STRINGS.DRAG,
                                vtScrollMM, hzScrollMM);
                        Networker.get().sendMemo(memo);
                    }

                    // Update the last point
                    mLastTouchPoint = new PointF(x, y);
                }


            }

            // RATE-BASED --------------------------------------------
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

                    // Only send amounts > 1 mm ( > ~ 1.2 px)
                    if (dYAbsMM > 0.5 || dXAbsMM > 0.5) {
                        Memo memo = new Memo(
                                STRINGS.SCROLL, STRINGS.RB,
                                vtScrollMM, hzScrollMM);
                        Networker.get().sendMemo(memo);
                    }

                    // Last point shouldn't change!
                }

            }

            mNumMovePoints++;

            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = mevent.getActionIndex();
            final int pointerId = mevent.getPointerId(pointerIndex);

            // If the left finger left the screen, find the next leftmost
            // IMPORTANT: the left finger still counts in "getPointerCount()"
            if (pointerId == mActivePointerId) {
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

                final float x = mevent.getX(newPointerIndex);
                final float y = mevent.getY(newPointerIndex);
                mLastTouchPoint = new PointF(x, y);

                mActivePointerId = mevent.getPointerId(newPointerIndex);

                if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED)) stopScroll();
            }

            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            if (mActiveTechnique.equals(TECHNIQUE.RATE_BASED)) stopScroll();
            break;
        }

        }
    }

    private void stopScroll() {
        Networker.get().sendMemo(MEMO_STOP_RB);
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
