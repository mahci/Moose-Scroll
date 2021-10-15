package at.aau.moose_scroll.controller;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class Actioner {
    private final String cName = "Actioner--";

    private static Actioner instance; // Singelton instance

    // Mode of scrolling
    private String mode = "Drag";

    // To find the right finger pointer
    private final int MAX_N_POINTERS = 5; // 5 diiff. touch pointers supported on most devices

    private int leftPointerID = INVALID_POINTER_ID; // Id of the left finger
    private double lastY;
    private int nTouchPoints; // = touchPointCounter in Demi's code

    // For Drag
    private final int SENSITIVITY = 2; // Count every n ACTION_MOVEs
    private final int GAIN = 1; // gainFactor in Demi's code

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
     * Perform the action
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    public void act(MotionEvent mevent, int mid) {
        int leftIndex, actionIndex;
        float leftDY;

        switch (mevent.getActionMasked()) {

                // Only one finger on the screen
            case MotionEvent.ACTION_DOWN:
                leftIndex = 0;
                leftPointerID = mevent.getPointerId(leftIndex); // Save pointer id
                lastY = mevent.getY();

                break;

                // More fingers are added
            case MotionEvent.ACTION_POINTER_DOWN:
                actionIndex = mevent.getActionIndex(); // Which pointer is down?

                // If new finger is on the left
                if (isLeftMost(mevent, actionIndex)) {
                    leftPointerID =  mevent.getPointerId(actionIndex); // Set ID
                    lastY = mevent.getY();
                }

                // TODO: add logging

            break;

            case MotionEvent.ACTION_MOVE:
                if (leftPointerID != INVALID_POINTER_ID) { // There is a leftmost finger on screen

                    leftIndex = mevent.findPointerIndex(leftPointerID);
                    if (leftIndex != -1) { // Valid id found
                        if (mode.equals("Drag")) drag(mevent, leftPointerID);
                        if (mode.equals("RB")) rateBased(mevent, leftPointerID);
                    }
                }

            break;
        }
    }

    /**
     * Perform the Drag scroll technique
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    private void drag(MotionEvent mevent, int mid) {
        String TAG = cName + "drag";

        if (nTouchPoints % SENSITIVITY == 0) {
            double dY = mevent.getY() - lastY;
            lastY = mevent.getY();

            double dragDelta = dY * GAIN * (-1); // -1 because of direction

            Log.d(TAG, "dragDelta = " + dragDelta);
            // TODO: send the dragDelta to the Server
        }
        nTouchPoints++;
    }

    /**
     * Perform the Rate-based scroll technique
     * @param mevent MotionEvent to process and perform
     * @param mid Unique id for the event
     */
    private void rateBased(MotionEvent mevent, int mid) {

    }

    /**
     * Check if a pointer is leftmost
     * @param me MortionEvent
     * @param pointerIndex index of the pointer to check
     * @return boolean
     */
    public boolean isLeftMost(MotionEvent me, int pointerIndex) {
        boolean result = true;
        for (int pix = 0; pix < me.getPointerCount(); pix++) {
            if (me.getX(pix) < me.getX(pointerIndex)) result = false;
        }

        return result;
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
