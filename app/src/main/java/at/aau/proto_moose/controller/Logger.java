package at.aau.proto_moose.controller;

import static at.aau.proto_moose.data.Consts.STRINGS.BLOCK;
import static at.aau.proto_moose.data.Consts.STRINGS.END;
import static at.aau.proto_moose.data.Consts.STRINGS.EXP_ID;
import static at.aau.proto_moose.data.Consts.STRINGS.SP;
import static at.aau.proto_moose.data.Consts.STRINGS.TECH;
import static at.aau.proto_moose.data.Consts.STRINGS.TRIAL;
import static at.aau.proto_moose.data.Consts.STRINGS.TSK;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import at.aau.proto_moose.data.Consts;
import at.aau.proto_moose.data.Memo;
import at.aau.proto_moose.tools.Logs;

import static android.view.MotionEvent.*;
import static at.aau.proto_moose.data.Consts.*;

public class Logger {
    private final static String NAME = "Logger/";

    private static Logger self;

    private static String mLogDirectory; // Main folder for logs

    private String mMotionEventLogPath;
    private PrintWriter mMotionEventLogPW;

    private GeneralInfo mGenInfo = new GeneralInfo();

    // -------------------------------------------------------------------------------------------
    public static Logger get() {
        if (self == null) self = new Logger();
        return self;
    }

    /**
     * Constructor
     */
    public Logger() {
        // Create the log dir (if not existed)
        mLogDirectory = Environment.getExternalStorageDirectory() + "/Moose_Scroll_Log/";
        boolean res = createDir(mLogDirectory);
        Logs.d(NAME, mLogDirectory, res);
    }

    /**
     * Create a dir if not existed
     * @param path Dir path
     * @return STATUS
     */
    public boolean createDir(String path) {
        File folder = new File(path);
        Logs.d(NAME, folder.exists());
        return folder.mkdir();
    }

    /**
     * Extract log info from Memo
     * @param memo Memo
     */
    public void setLogInfo(Memo memo) {
        switch (memo.getMode()) {
            case EXP_ID: {
                logExperimentStart(memo.getValue1());
            }

            case TECH + "_" + TSK: {
                mGenInfo.tech = TECHNIQUE.get(memo.getValue1Int());
                mGenInfo.task = TASK.get(memo.getValue2Int());
            }

            case BLOCK + "_" + TRIAL: {
                mGenInfo.blockNum = memo.getValue1Int();
                mGenInfo.trialNum = memo.getValue2Int();
            }

            case END: {
                closeLogs();
            }

        }
    }


    /**
     * Log the start of an experiment
     * @param expLogId String containing the experiment info
     */
    public void logExperimentStart(String expLogId) {
        final String TAG = NAME + "logParticipant";

        mMotionEventLogPath = mLogDirectory + "/" + expLogId + "_" + "LOG.txt";

        try {
            mMotionEventLogPW = new PrintWriter(new FileWriter(mMotionEventLogPath, false));
            mMotionEventLogPW.println(
                    GeneralInfo.getLogHeader() + SP + MotionEventInfo.getLogHeader());
            mMotionEventLogPW.flush();

        } catch (IOException e) {
            Logs.d(TAG, "Error in creating participant file!");
            e.printStackTrace();
        }
    }

    /**
     * Log MotionEventInfo
     * @param meventInfo MotionEventInfo
     */
    public void logMotionEventInfo(MotionEventInfo meventInfo) {
        try {
            if (mMotionEventLogPW == null) { // Open only if not opened before
                mMotionEventLogPW = new PrintWriter(
                        new FileWriter(mMotionEventLogPath, true));
            }

            mMotionEventLogPW.println(mGenInfo + SP + meventInfo);
            mMotionEventLogPW.flush();

        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close all the log files
     */
    public void closeLogs() {
        if (mMotionEventLogPW != null) mMotionEventLogPW.close();
    }

    /**
     * Get the input with #.###
     * @param input double
     * @return String
     */
    @SuppressLint("DefaultLocale")
    public static String double3Dec(double input) {
        return String.format("%.3f", input);
    }

    /**
     * Get the string for a MotionEvent.PointerCoord
     * @return String (semi-colon separated)
     */
    public static String pointerCoordsToStr(PointerCoords inPC) {
        return double3Dec(inPC.orientation) + SP + 
                double3Dec(inPC.pressure) + SP + 
                double3Dec(inPC.size) + SP +
                double3Dec(inPC.toolMajor) + SP + 
                double3Dec(inPC.toolMinor) + SP + 
                double3Dec(inPC.touchMajor) + SP + 
                double3Dec(inPC.touchMinor) + SP + 
                double3Dec(inPC.x) + SP + 
                double3Dec(inPC.y);

    }

    /**
     * Truly GET the PointerCoords!
     * @param me MotionEvent
     * @param pointerIndex int pointer index
     * @return String
     */
    public static String pointerCoordsToStr(MotionEvent me, int pointerIndex) {
        PointerCoords result = new PointerCoords();
        me.getPointerCoords(pointerIndex, result);
        return pointerCoordsToStr(result);
    }

    // -------------------------------------------------------------------------------------------
    // General info
    public static class GeneralInfo {
        public Consts.TECHNIQUE tech;
        public Consts.TASK task;
        public int blockNum;
        public int trialNum;

        public static String getLogHeader() {
            return "technique" + SP +
                    "task" + SP +
                    "block_num" + SP +
                    "trial_num" + SP;
        }

        @NonNull
        @Override
        public String toString() {
            return tech + SP +
                    task + SP +
                    blockNum + SP +
                    trialNum + SP;
        }
    }

    // MotionEvent info
    public static class MotionEventInfo {
        public MotionEvent event;

        public MotionEventInfo(MotionEvent me) {
            event = me;
        }

        public static String getLogHeader() {
            return "action" + SP +

                    "flags" + SP +
                    "edge_flags" + SP +
                    "source" + SP +

                    "event_time" + SP +
                    "down_time" + SP +

                    "number_pointers" + SP +

                    "finger_1_index" + SP +
                    "finger_1_id" + SP +
                    "finger_1_orientation" + SP +
                    "finger_1_pressure" + SP +
                    "finger_1_size" + SP +
                    "finger_1_toolMajor" + SP +
                    "finger_1_toolMinor" + SP +
                    "finger_1_touchMajor" + SP +
                    "finger_1_touchMinor" + SP +
                    "finger_1_x" + SP +
                    "finger_1_y" + SP +

                    "finger_2_index" + SP +
                    "finger_2_id" + SP +
                    "finger_2_orientation" + SP +
                    "finger_2_pressure" + SP +
                    "finger_2_size" + SP +
                    "finger_2_toolMajor" + SP +
                    "finger_2_toolMinor" + SP +
                    "finger_2_touchMajor" + SP +
                    "finger_2_touchMinor" + SP +
                    "finger_2_x" + SP +
                    "finger_2_y" + SP +

                    "finger_3_index" + SP +
                    "finger_3_id" + SP +
                    "finger_3_orientation" + SP +
                    "finger_3_pressure" + SP +
                    "finger_3_size" + SP +
                    "finger_3_toolMajor" + SP +
                    "finger_3_toolMinor" + SP +
                    "finger_3_touchMajor" + SP +
                    "finger_3_touchMinor" + SP +
                    "finger_3_x" + SP +
                    "finger_3_y" + SP +

                    "finger_4_index" + SP +
                    "finger_4_id" + SP +
                    "finger_4_orientation" + SP +
                    "finger_4_pressure" + SP +
                    "finger_4_size" + SP +
                    "finger_4_toolMajor" + SP +
                    "finger_4_toolMinor" + SP +
                    "finger_4_touchMajor" + SP +
                    "finger_4_touchMinor" + SP +
                    "finger_4_x" + SP +
                    "finger_4_y" + SP +

                    "finger_5_index" + SP +
                    "finger_5_id" + SP +
                    "finger_5_orientation" + SP +
                    "finger_5_pressure" + SP +
                    "finger_5_size" + SP +
                    "finger_5_toolMajor" + SP +
                    "finger_5_toolMinor" + SP +
                    "finger_5_touchMajor" + SP +
                    "finger_5_touchMinor" + SP +
                    "finger_5_x" + SP +
                    "finger_5_y";
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();

            result.append(event.getActionMasked()).append(SP);

            result.append("0x").append(Integer.toHexString(event.getFlags())).append(SP);
            result.append("0x").append(Integer.toHexString(event.getEdgeFlags())).append(SP);
            result.append("0x").append(Integer.toHexString(event.getSource())).append(SP);

            result.append(event.getEventTime()).append(SP);
            result.append(event.getDownTime()).append(SP);

            // Pointers' info (for 0 - (nPointer -1) => real values | for the rest to 5 => dummy)
            int nPointers = event.getPointerCount();
            result.append(nPointers).append(SP);
            int pi;
            for(pi = 0; pi < nPointers; pi++) {
                result.append(pi).append(SP); // Index
                result.append(event.getPointerId(pi)).append(SP); // Id
                // PointerCoords
                result.append(pointerCoordsToStr(event, pi)).append(SP);
            }

            for (pi = nPointers; pi < 5; pi++) {
                result.append(-1).append(SP); // Index = -1
                result.append(-1).append(SP); // Id = -1
                // PointerCoords = empty
                result.append(pointerCoordsToStr(new MotionEvent.PointerCoords()))
                        .append(SP);
            }

            String resStr = result.toString();
            return resStr.substring(0, resStr.length() - 1); // Remove the last SP
        }
    }



}
