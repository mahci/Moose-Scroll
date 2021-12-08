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
    private TECH technique = TECH.DRAG;
    private MODE mode = MODE.TWOD;

    // Algorithm parameters
    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
    private int leftmostIndex = INVALID_POINTER_ID; // Index of the leftmost finger
    private int actionIndex = INVALID_POINTER_ID; // New finger's index
    private PointF lastPoint;
    private int nTouchPoints; // = touchPointCounter in Demi's code

    // Config
    private final int DRAG_SENSITIVITY = 2; // Count every n ACTION_MOVEs (drag)
    private final int RB_SENSITIVITY = 2; // Count every n ACTION_MOVEs (rate-based)

    private final int DENOM_RB = 10; // Denominator in RB's speed formula

    private final double GAIN_DRAG = 1; // Gain factor for drag
    private final double GAIN_RB = 1; // Gain factor for rate-based

    private final int PPI = 312; // For calculating movement in mm

    private final Memo MEMO_STOP_RB = new Memo(
            STRINGS.SCROLL, STRINGS.RB,
            STRINGS.STOP, STRINGS.STOP);


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
     * Set the mode of scrolling
     * @param m Mode
     */
    public void setMode(MODE m) {
        mode = m;
    }

    /**
     * Perform the action
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    public void act(MotionEvent mevent, int mid) {
        String TAG = NAME + "act";

//        if (technique.equals(TECH.DRAG)) scrollDrag(mevent, mid);
//        if (technique.equals(TECH.RATE_BASED)) scrollRateBased(mevent, mid);


    }

    /**
     * Reset the scrolling
     */
    private void resetScroll() {
        // Mode doens't matter here
        Networker.get().sendMemo(new Memo(STRINGS.SCROLL, STRINGS.RB, 0, 0));
        nTouchPoints = 0;
    }

    /**
     * Perform the Drag scroll technique
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    private void scrollDrag(MotionEvent mevent, int mid) {
        String TAG = NAME + "drag";

        actionIndex = mevent.getActionIndex(); // Ignored in MOVE

        switch (mevent.getActionMasked()) {
        // Only one finger on the screen
        case MotionEvent.ACTION_DOWN:
            nTouchPoints = 0;
            break;

        // More fingers are added
        case MotionEvent.ACTION_POINTER_DOWN:
            nTouchPoints = 0;
//            if (mevent.getX(actionIndex) < mevent.getX(leftmostIndex)) {
//                leftmostIndex = actionIndex;
//            }
            break;

        // One finger is up
        case MotionEvent.ACTION_POINTER_UP:
            // Left finger up, recalculate the leftmostIndex
            if (actionIndex == leftmostIndex) {
                nTouchPoints = 0;
                leftmostIndex = 0;
                for (int pi = 1; pi < mevent.getPointerCount(); pi++) {
                    if (pi != actionIndex && mevent.getX(pi) < mevent.getX(leftmostIndex)) {
                        leftmostIndex = pi;
                    }
                }
            }

            break;

        // Last finger up
        case MotionEvent.ACTION_UP:
            nTouchPoints = 0;
            break;

        // Moving the fingers on the screen
        case MotionEvent.ACTION_MOVE:
            // actionIndex = mevent.getActionIndex(); // DOESN'T WORK FOR MOVE!!
            leftmostIndex = findLeftMostIndex(mevent);

            int lmInd = 0;
            for (int pi = 0; pi < mevent.getPointerCount(); pi++) {
                if (mevent.getX(pi) < mevent.getX(lmInd)) lmInd = pi;
            }

            if (lastPoint == null) { // Start of movement
                lastPoint = new PointF(mevent.getX(lmInd), mevent.getY(lmInd));
            } else {
                    if (nTouchPoints % DRAG_SENSITIVITY == 0) {
                        double dX = mevent.getX() - lastPoint.x;
                        double dY = mevent.getY() - lastPoint.y;

                        double scrollDXMM = px2mm(dX * GAIN_DRAG);
                        double scrollDYMM = px2mm(dY * GAIN_DRAG);

                        if (mode.equals(MODE.VERTICAL)) {
                            // Send the movement to server (y/vertical first value)
                            Memo vtMemo = new Memo(STRINGS.SCROLL, STRINGS.DRAG, scrollDYMM, 0);
                            Networker.get().sendMemo(vtMemo);
                        }

                        if (mode.equals(MODE.TWOD)) {
                            // Send the movement to server (y/vertical first value)
                            Memo tdMemo = new Memo(STRINGS.SCROLL, STRINGS.DRAG, scrollDYMM, scrollDXMM);
                            Networker.get().sendMemo(tdMemo);
                        }

                        // Update the points
                        lastPoint = new PointF(mevent.getX(), mevent.getY());
                    }

                    nTouchPoints++;
                }

            break;

        }
    }

    /**
     * Perform the Rate-based scroll technique
     * @param mevent MotionEvent to process and perform
     * @param lmIndex Index of the moving finger
     */
    private void scrollRateBased(MotionEvent mevent, int lmIndex) {
        String TAG = NAME + "drag";

        actionIndex = mevent.getActionIndex(); // Ignored in MOVE

        switch (mevent.getActionMasked()) {
        // Only one finger on the screen
        case MotionEvent.ACTION_DOWN:
//            leftmostIndex = 0;
            Networker.get().sendMemo(MEMO_STOP_RB);
            nTouchPoints = 0;
            lastPoint = null;
        break;

        // More fingers are added
        case MotionEvent.ACTION_POINTER_DOWN:
            Networker.get().sendMemo(MEMO_STOP_RB);
            nTouchPoints = 0;
            lastPoint = null;
//            if (mevent.getX(actionIndex) < mevent.getX(leftmostIndex)) {
//                leftmostIndex = actionIndex;
//            }
        break;

        // One finger is up
        case MotionEvent.ACTION_POINTER_UP:
            Networker.get().sendMemo(MEMO_STOP_RB);
            nTouchPoints = 0;
            lastPoint = null;
            // Left finger up, recalculate the leftmostIndex
//            if (actionIndex == leftmostIndex) {
//                Networker.get().sendMemo(MEMO_STOP_RB);
//                nTouchPoints = 0;
//                leftmostIndex = 0;
//                for (int pi = 1; pi < mevent.getPointerCount(); pi++) {
//                    if (pi != actionIndex && mevent.getX(pi) < mevent.getX(leftmostIndex)) {
//                        leftmostIndex = pi;
//                    }
//                }
//            }
//            Logs.d(TAG, "leftmostIndex", leftmostIndex);

        break;

        // Last finger up
        case MotionEvent.ACTION_UP:
            nTouchPoints = 0;
            lastPoint = null;
            Networker.get().sendMemo(MEMO_STOP_RB);
        break;

        // Moving the fingers on the screen
        case MotionEvent.ACTION_MOVE:

            // Only count the movement of the leftmost finger
            int lmInd = 0;
            for (int pi = 0; pi < mevent.getPointerCount(); pi++) {
                if (mevent.getX(pi) < mevent.getX(lmInd)) lmInd = pi;
            }

            if (lastPoint == null) { // Start of movement
                lastPoint = new PointF(mevent.getX(lmInd), mevent.getY(lmInd));
            } else { // Continuation of movement
                if (nTouchPoints % RB_SENSITIVITY == 0) {
                    double dX = mevent.getX(lmInd) - lastPoint.x;
                    double dY = mevent.getY(lmInd) - lastPoint.y;

                    double absDX = Math.pow(Math.abs(dX), GAIN_RB) / DENOM_RB; // My version
                    double absDXMM = px2mm(absDX);
                    int dirDX = (int) (dX / Math.abs(dX));

                    double absDY = Math.pow(Math.abs(dY), GAIN_RB) / DENOM_RB; // My version
                    double absDYMM = px2mm(absDY);
                    int dirDY = (int) (dY / Math.abs(dY));
                    Logs.d(TAG, "Deltas(mm)", absDYMM, absDXMM);
                    if (mode.equals(MODE.VERTICAL)) {
                        // Only send amounts > 0.1 mm ( > ~ 1.2 px)
                        if (absDXMM > 0.1) {
                            Networker.get().sendMemo(
                                    new Memo(STRINGS.SCROLL, STRINGS.RB,
                                            absDYMM * dirDY, 0)
                            );
                        }
                    }

                    if (mode.equals(MODE.TWOD)) {
                        // Only send amounts > 0.1 mm ( > ~ 1.2 px)
                        if (absDXMM > 0.1 || absDYMM > 0.1) {
                            Networker.get().sendMemo(
                                    new Memo(STRINGS.SCROLL, STRINGS.RB,
                                            absDYMM * dirDY, absDXMM * dirDX)
                            );
                        }
                    }

                }

                nTouchPoints++;
            }


        break;

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

    private double px2mm(double px) {
        return (px / PPI) * 25.4;
    }

}
