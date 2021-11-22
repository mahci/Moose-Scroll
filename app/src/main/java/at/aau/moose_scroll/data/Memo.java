package at.aau.moose_scroll.data;

import static at.aau.moose_scroll.data.Consts.STRINGS.*;

import android.util.Log;

public class Memo {
    private static final String NAME = "Memo/";

    private String action;
    private String mode;
    private String valueX;
    private String valueY;

    /**
     * Constructor
     * @param act Action Mostly "SCROLL"
     * @param md Mode DRAG or RT
     * @param vlX String value Movement along X
     * @param vlY String value Movement along Y
     */
    public Memo(String act, String md, String vlX, String vlY) {
        action = act;
        mode = md;
        valueX = vlX;
        valueY = vlY;
    }

    /**
     * Constructor
     * @param act Action Mostly "SCROLL"
     * @param md Mode DRAG or RT
     * @param vlX Double value Movement along X
     * @param vlY Double value Movement along Y
     */
    public Memo(String act, String md, double vlX, double vlY) {
        action = act;
        mode = md;
        valueX = String.valueOf(vlX);
        valueY = String.valueOf(vlY);
    }

    /**
     * Basic consrtuctor
     */
    public Memo() {
        action = "";
        mode = "";
        valueX = "";
        valueY = "";
    }

    /**
     * Return action
     * @return String Action
     */
    public String getAction() {
        return action;
    }

    /**
     * Return mode
     * @return String Mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Convert and return the X value
     * @return Int X value
     */
    public int getValueXInt() {
        try {
            return (int) Double.parseDouble(valueX);
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    /**
     * Convert and return the value
     * @return Int Y Value
     */
    public int getValueYInt() {
        try {
            return (int) Double.parseDouble(valueX);
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    /**
     * Get the Memo from String
     * @param mssg String
     * @return Memo
     */
    public static Memo valueOf(String mssg) {
        String TAG = NAME + "valueOf";

        Memo result = new Memo();
        if (mssg != null) {
            String[] parts = mssg.split(SP);
            if (parts.length == 4) {
                result.action = parts[0];
                result.mode = parts[1];
                result.valueX = parts[2];
                result.valueY = parts[3];
            } else {
                Log.d(TAG, "Problem in parsing the memo!");
            }
        }

        return result;
    }

    /**
     * Get the String equivaluent
     * @return String
     */
    @Override
    public String toString() {
        return action + SP + mode + SP + valueX + SP + valueY;
    }
}
