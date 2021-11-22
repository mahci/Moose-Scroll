package at.aau.moose_scroll.data;

import static at.aau.moose_scroll.data.Consts.STRINGS.*;

import android.util.Log;

public class Memo {
    private static final String NAME = "Memo--";

    private String action;
    private String mode;
    private String value;

    /**
     * Constructor
     * @param act Action
     * @param md Mode
     * @param vl Value
     */
    public Memo(String act, String md, String vl) {
        action = act;
        mode = md;
        value = vl;
    }

    /**
     * Basic consrtuctor
     */
    public Memo() {
        action = "";
        mode = "";
        value = "";
    }

    public String getAction() {
        return action;
    }

    public String getMode() {
        return mode;
    }

    /**
     * Return value of the Memo
     * @return Value
     */
    public String getValue() {
        return value;
    }

    /**
     * Convert and return the value
     * @return Value (int)
     */
    public int getValueInt() {
        try {
            return (int) Double.parseDouble(value);
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
            if (parts.length == 3) {
                result.action = parts[0];
                result.mode = parts[1];
                result.value = parts[2];
            } else {
                Log.d(TAG, "Problem in parsing the memo!");
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return action + SP + mode + SP + value;
    }
}
