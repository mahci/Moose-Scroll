package at.aau.moose_scroll.data;

import static at.aau.moose_scroll.data.Consts.STRINGS.*;

import java.util.Arrays;

import at.aau.moose_scroll.experiment.Experiment;
import at.aau.moose_scroll.tools.Logs;

public class Memo {
    private static final String NAME = "Memo/";

    private String action;
    private String mode;
    private String value1;
    private String value2;

//    public static Memo RB_STOP_MEMO = new Memo(SCROLL, Experiment.TECHNIQUE.RATE_BASED, STOP, STOP);

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 String value1
     * @param v2 String value2
     */
    public Memo(String act, String md, String v1, String v2) {
        action = act;
        mode = md;
        value1 = v1;
        value2 = v2;
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Double value Movement along X
     * @param v2 Double value Movement along Y
     */
    public Memo(String act, String md, double v1, double v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param tech TECHNIQUE Mode (e.g. DRAG)
     * @param v1 Double value Movement along X
     * @param v2 Double value Movement along Y
     */
    public Memo(String act, Experiment.TECHNIQUE tech, double v1, double v2) {
        this(act, tech.toString(), v1, v2);
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     */
    public Memo(String act, String md, Object v1) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = "-";
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     * @param v2 Int value 2
     */
    public Memo(String act, String md, int v1, int v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Constructor
     * @param act Action (e.g. SCROLL)
     * @param md Mode (e.g. DRAG)
     * @param v1 Int value 1
     * @param v2 Int value 2
     */
    public Memo(String act, String md, Object v1, Object v2) {
        action = act;
        mode = md;
        value1 = String.valueOf(v1);
        value2 = String.valueOf(v2);
    }

    /**
     * Basic consrtuctor
     */
    public Memo() {
        action = "";
        mode = "";
        value1 = "";
        value2 = "";
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
     * Get the first value
     * @return String
     */
    public String getValue1Str() {
        return value1;
    }

    /**
     * Get the second value
     * @return String
     */
    public String getValue2Str() {
        return value2;
    }

    /**
     * Convert and return the X value
     * @return Int X value
     */
    public int getValue1Int() {
        try {
            return (int) Double.parseDouble(value1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getValue1Double() {
        try {
            return Double.parseDouble(value1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getValue2Double() {
        try {
            return Double.parseDouble(value2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convert and return the value
     * @return Int Y Value
     */
    public int getValue2Int() {
        try {
            return (int) Double.parseDouble(value2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isStopMemo() {
        return getValue1Str().equals(STOP);
    }

    /**
     * Get the Memo from String
     * @param mssg String
     * @return Memo
     */
    public static Memo fromString(String mssg) {
        String TAG = NAME + "valueOf";

        Memo result = new Memo();
        if (mssg != null) {
            String[] parts = mssg.split(MEMOSP);
            Logs.d(TAG, Arrays.toString(parts));
            if (parts.length == 4) {
                result.action = parts[0];
                result.mode = parts[1];
                result.value1 = parts[2];
                result.value2 = parts[3];
            } else {
                Logs.d(TAG, "Problem in parsing the Memo!");
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
        return action + MEMOSP + mode + MEMOSP + value1 + MEMOSP + value2;
    }
}
