package at.aau.moose_scroll.controller;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import static at.aau.moose_scroll.data.Consts.*;

import android.graphics.PointF;
import android.view.MotionEvent;

import at.aau.moose_scroll.data.Consts.*;
import at.aau.moose_scroll.data.Memo;
import at.aau.moose_scroll.tools.Logs;


public class Actioner {
    private final String NAME = "Actioner--";
    // -------------------------------------------------------------------------------

    private static Actioner instance; // Singelton instance

    // Mode of scrolling
    private TECH technique = TECH.DRAG;
    private MODE mode = MODE.TWOD;

    // Algorithm parameters
    private int leftmostId = INVALID_POINTER_ID; // Id of the left finger
    private PointF lastPoint;
    private int nTouchPoints; // = touchPointCounter in Demi's code

    // Config
    private final int DRAG_SENSITIVITY = 5; // Count every n ACTION_MOVEs (drag)
    private final int RB_SENSITIVITY = 1; // Count every n ACTION_MOVEs (rate-based)

    private final int DENOM_RB = 10; // Denominator in RB's speed formula

    private final double GAIN_DRAG = 1; // Gain factor for drag
    private final double GAIN_RB = 1; // Gain factor for rate-based

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
        int leftmostIndex = INVALID_POINTER_ID;

        switch (mevent.getActionMasked()) {

                // Only one finger on the screen
            case MotionEvent.ACTION_DOWN:
                updatePointers(mevent);

                Logs.d(TAG, "DOWN ID= " + leftmostId + " | Index= " + leftmostIndex);

                break;

                // More fingers are added
            case MotionEvent.ACTION_POINTER_DOWN:

                // If new finger is on the left, update
                if (isLeftMost(mevent, mevent.getActionIndex())) {
                    resetScroll();
                    updatePointers(mevent);
                }

                Logs.d(TAG, "PDOWN ID= " + leftmostId + " | Index= " + leftmostIndex);

            break;

                // Moving the fingers on the screen
            case MotionEvent.ACTION_MOVE:
                // actionIndex = mevent.getActionIndex(); // DOESN'T WORK FOR MOVE!!
                leftmostIndex = findLeftMostIndex(mevent);
                Logs.d(TAG, "MOVE ID= " + leftmostId + " | Index= " + leftmostIndex);
                switch (technique) {
                case DRAG: scrollDrag(mevent, leftmostIndex); break;
                case RATE_BASED: scrollRateBased(mevent, leftmostIndex); break;
                }

            break;
        // One finger is up

        case MotionEvent.ACTION_POINTER_UP:

            // If new finger is on the left, update
            if (isLeftMost(mevent, mevent.getActionIndex())) {
                resetScroll();
                updatePointers(mevent);
            }

            Logs.d(TAG, "PUP ID= " + leftmostId + " | Index= " + leftmostIndex);

            break;

            // Last finger up
        case MotionEvent.ACTION_UP:
            resetScroll();

            Logs.d(TAG, "UP ID= " + leftmostId + " | Index= " + leftmostIndex);

            break;
        }
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

        switch (mode) {
        case VERTICAL:
            if (nTouchPoints % DRAG_SENSITIVITY == 0) {
                double dX = mevent.getX() - lastPoint.x;
                double dY = mevent.getY() - lastPoint.y;

                double scrollDetla = dY * GAIN_DRAG * (-1); // (-1) for the direction

                // TODO: Put limit on dY
                // Send the message via the Networker
//                Networker.get().sendMemo(new Memo(SCROLL, DRAG, String.valueOf(scrollDetla)));

                // Update the touch point
                lastPoint = new PointF(mevent.getX(), mevent.getY());
            }

            break;
        case HORIZONTAL:
            if (nTouchPoints % DRAG_SENSITIVITY == 0) {
                double dX = mevent.getX() - lastPoint.x;
                double dY = mevent.getY() - lastPoint.y;

                double scrollDetla = dX * GAIN_DRAG * (-1); // (-1) for the direction

                // TODO: Put limit on dY
                // Send the message via the Networker
//                Networker.get().sendMemo(new Memo(SCROLL, DRAG, String.valueOf(scrollDetla)));

                // Update the touch point
                lastPoint = new PointF(mevent.getX(), mevent.getY());
            }
            break;

        case TWOD:

            if (nTouchPoints % DRAG_SENSITIVITY == 0) {
                double dX = mevent.getX() - lastPoint.x;
                double dY = mevent.getY() - lastPoint.y;

                double scrollDX = dX * GAIN_DRAG;
                double scrollDY = dY * GAIN_DRAG;

                // Send the movement to server
                Memo tdMemo = new Memo(STRINGS.SCROLL, STRINGS.DRAG, scrollDX, scrollDY);
                Networker.get().sendMemo(tdMemo);
            }
            break;
        }

        nTouchPoints++;
    }

    /**
     * Perform the Rate-based scroll technique
     * @param mevent MotionEvent to process and perform
     * @param lmIndex Index of the moving finger
     */
    private void scrollRateBased(MotionEvent mevent, int lmIndex) {
        String TAG = NAME + "drag";

        switch (mode) {
        case VERTICAL:

            if (nTouchPoints % RB_SENSITIVITY == 0) {
                double dX = mevent.getX(lmIndex) - lastPoint.x;
                double dY = mevent.getY(lmIndex) - lastPoint.y;
                // [ATTENTION] lastPoint stays the same during the action!

                // Calculate the scroll delta
//                double absDelta = Math.pow(Math.abs(dX), GAIN_RB) / 1000; // Demi's
                double absDelta = Math.pow(Math.abs(dY), GAIN_RB) / DENOM_RB; // My version
                int direction = (int) (dY / Math.abs(dY)) * (-1); // For direction

                double scrollDelta = absDelta * direction;

                // Send the message via the Networker
//                Networker.get().sendMemo(new Memo(SCROLL, RB, String.valueOf(scrollDelta)));

            }

            break;
        case HORIZONTAL:

            if (nTouchPoints % RB_SENSITIVITY == 0) {
                double dX = mevent.getX(lmIndex) - lastPoint.x;
                double dY = mevent.getY(lmIndex) - lastPoint.y;
                // [ATTENTION] lastPoint stays the same during the action!

                // Calculate the scroll delta
//                double absDelta = Math.pow(Math.abs(dX), GAIN_RB) / 1000; // Demi's
                double absDelta = Math.pow(Math.abs(dX), GAIN_RB) / DENOM_RB; // My version
                int direction = (int) (dX / Math.abs(dX)) * (-1); // For direction

                double scrollDelta = absDelta * direction;

                // Send the message via the Networker
//                Networker.get().sendMemo(new Memo(SCROLL, RB, String.valueOf(scrollDelta)));

            }
            break;
        }

        nTouchPoints++;
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
        int nPointers = me.getPointerCount();
        if (nPointers == 0) return -1;
        if (nPointers == 1) return  0;

        // > 1 pointers on the screen
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

        int leftmostIndex = findLeftMostIndex(me);
        leftmostId = me.getPointerId(leftmostIndex);
        lastPoint = new PointF(me.getX(leftmostIndex), me.getY(leftmostIndex));

        Logs.d(TAG, "lmId= " + leftmostIndex + " | " + leftmostIndex);
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

}
