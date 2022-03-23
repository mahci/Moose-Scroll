package at.aau.moose_scroll.controller;

import static at.aau.moose_scroll.data.Consts.STRINGS.END;
import static at.aau.moose_scroll.data.Consts.STRINGS.EXPID;
import static at.aau.moose_scroll.data.Consts.STRINGS.GENINFO;
import static at.aau.moose_scroll.data.Consts.STRINGS.SP;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import at.aau.moose_scroll.data.Memo;
import at.aau.moose_scroll.experiment.Trial;
import at.aau.moose_scroll.tools.Logs;

import static android.view.MotionEvent.*;
import static at.aau.moose_scroll.experiment.Experiment.*;

public class Logger {
    private final static String NAME = "Logger/";

    private static Logger self;

    private static String mLogDirectory; // Main folder for logs
    private static String mPcLogDir; // Log dir for the ptc

    private String mMotionEventLogPath;
    private PrintWriter mMotionEventLogPW;

    private GeneralInfo mGenInfo;

    private boolean mLogFilesOpen = false;
    private boolean mReadyToLog = false;

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
        final String TAG = NAME + "setLogInfo";

        Logs.d(TAG, memo.getMode());
        switch (memo.getMode()) {

        case EXPID: {
            logExperimentInfo(memo.getValue1Str(), memo.getValue2Str());
            break;
        }

        case GENINFO: {
            closeLogs();
            mGenInfo = new GeneralInfo(memo.getValue1Str());
            mReadyToLog = true;
            Logs.d(TAG, mGenInfo);
            break;
        }

        case END: {
            closeLogs();
            mReadyToLog = false;
            Logs.d(TAG, "Seriously?!!");
            break;
        }

        }

        Logs.d(TAG, mReadyToLog);
    }


    /**
     * Log the start of an experiment
     * @param pcLogId String containing id of the participant (e.g. P2)
     * @param expLogId String containing the experiment info (e.g. P5_18-03-2022)
     */
    public void logExperimentInfo(String pcLogId, String expLogId) {
        final String TAG = NAME + "logParticipant";

        mPcLogDir = mLogDirectory + "/" + pcLogId;
        mMotionEventLogPath = mPcLogDir + "/" + expLogId + "_" + "MOEV.txt";
        createLogFiles();
    }

    public void createLogFiles() {
        final String TAG = NAME + "createLogFiles";

        try {
            // Create a directory for the ptc
            boolean res = createDir(mPcLogDir);

            // Create the log files
            mMotionEventLogPW = new PrintWriter(new FileWriter(mMotionEventLogPath, true));
            mMotionEventLogPW.println(
                    GeneralInfo.getLogHeader() + SP + MotionEventInfo.getLogHeader());
            mMotionEventLogPW.flush();
            mMotionEventLogPW.close();

            mLogFilesOpen = false;

            Logs.d(TAG, "log file created!");
        } catch (IOException e) {
            Logs.d(TAG, "Error in exp. info logging!");
            e.printStackTrace();
        }
    }

    /**
     * Log MotionEventInfo
     * @param meventInfo MotionEventInfo
     */
    public void logMotionEventInfo(MotionEventInfo meventInfo) {
        final String TAG = NAME + "logMotionEventInfo";
        Logs.d(TAG, "Ready?", mReadyToLog);
        if (mReadyToLog) {
            try {
                if (new File(mMotionEventLogPath).isFile()) {

                    if (!mLogFilesOpen) {
                        final FileWriter logFW = new FileWriter(mMotionEventLogPath, true);
                        mMotionEventLogPW = new PrintWriter(logFW);

                        mLogFilesOpen = true;
                    }

                    mMotionEventLogPW.println(mGenInfo + SP + meventInfo);
                    mMotionEventLogPW.flush();
                    Logs.d(TAG, "Motion logged");

                } else { // log file doesn't exist
                    Logs.d(TAG, "File doesn't exist");
                    createLogFiles();
                }

            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close all the log files
     */
    public void closeLogs() {
        final String TAG = NAME + "closeLogs";
        Logs.d(TAG, mMotionEventLogPW);
        if (mMotionEventLogPW != null) mMotionEventLogPW.close();
        mLogFilesOpen = false;
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
        public int session;
        public int part;
        public TECHNIQUE tech;
        public int blockNum;
        public int trialNum;
        public Trial trial = new Trial();

        /**
         * Create the object from a serialized String (exact output of toString())
         * @param szdStr Serialized String
         */
        public GeneralInfo(String szdStr) {
            String[] splitStr = szdStr.split(SP);
            if (splitStr.length == 10) {
                session = Integer.parseInt(splitStr[0]);
                part = Integer.parseInt(splitStr[1]);
                tech = TECHNIQUE.valueOf(splitStr[2]);
                blockNum = Integer.parseInt(splitStr[3]);
                trialNum = Integer.parseInt(splitStr[4]);

                trial = new Trial();
                trial.task = TASK.valueOf(splitStr[5]);
                trial.direction = DIRECTION.valueOf(splitStr[6]);
                trial.vtDist = Integer.parseInt(splitStr[7]);
                trial.tdDist = Integer.parseInt(splitStr[8]);
                trial.frame = Integer.parseInt(splitStr[9]);
            } else {
                Logs.e("GeneralInfo", "Num. of parts not 10");
            }
        }

        public static String getLogHeader() {
            return "session" + SP +
                    "part" + SP +
                    "technique" + SP +
                    "block_num" + SP +
                    "trial_num" + SP +
                    Trial.getLogHeader();
        }

        @Override
        public String toString() {
            return session + SP +
                    part + SP +
                    tech + SP +
                    blockNum + SP +
                    trialNum + SP +
                    trial.toLogString();
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
