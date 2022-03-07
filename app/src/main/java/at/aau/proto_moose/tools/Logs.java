package at.aau.proto_moose.tools;

import android.util.Log;

/**
 * A kinda wrapper class for Log
 */
public class Logs {

    /**
     * Show a debug log
     * @param tag Tag to show
     * @param params Parameters
     */
    public static void d(String tag, Object... params) {
        final int pLen = params.length;
        if (pLen > 0) {
            StringBuilder sb = new StringBuilder();
            for(int oi = 0; oi < pLen - 1; oi++) {
                sb.append(params[oi]).append(" | ");
            }
            sb.append(params[pLen - 1]);

            System.out.println(tag + " >> " + sb);
        }
    }

    /**
     * Show error log
     * @param tag TAG
     * @param params Things to show
     */
    public static void e(String tag, Object... params) {
        final int pLen = params.length;
        if(pLen > 0) {
            StringBuilder sb = new StringBuilder();
            for(int oi = 0; oi < pLen - 1; oi++) {
                sb.append(params[oi]).append(" | ");
            }
            System.out.println(tag + " !! " + sb.append(pLen - 1));
        }
    }

}
