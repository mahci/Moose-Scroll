package at.aau.moose_scroll.tools;

import android.util.Log;

/**
 * A kinda wrapper class for Log
 */
public class Logs {

    public static void d(String tag, String mssg) {
        Log.d(tag, mssg);
    }

    public static void d(String tag, int mssg) {
        Logs.d(tag, String.valueOf(mssg));
    }

    public static void d(String tag, double mssg) {
        Logs.d(tag, String.valueOf(mssg));
    }

}
