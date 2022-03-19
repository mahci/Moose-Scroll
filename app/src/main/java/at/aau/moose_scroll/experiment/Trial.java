package at.aau.moose_scroll.experiment;

import at.aau.moose_scroll.tools.Logs;

import static at.aau.moose_scroll.data.Consts.STRINGS.SP;
import static at.aau.moose_scroll.experiment.Experiment.*;

public class Trial {
    public TASK task;
    public DIRECTION direction;
    public int vtDist;
    public int tdDist;
    public int frame;

    /**
     * Empty constructor
     */
    public Trial() {

    }

    /**
     * Create the object from a serialized String (exact output of toString())
     * @param szdStr Serialized String
     */
    public Trial(String szdStr) {
        Logs.d("Trial", szdStr);
        String[] splitStr = szdStr.split(SP);
        if (splitStr.length == 5) {
            task = TASK.valueOf(splitStr[0]);
            direction = DIRECTION.valueOf(splitStr[1]);
            vtDist = Integer.parseInt(splitStr[2]);
            tdDist = Integer.parseInt(splitStr[3]);
            frame = Integer.parseInt(splitStr[4]);
        } else {
            Logs.e("Trial", "Problem in creation with String");
        }
    }

    public String toLogString() {
        return task + SP +
                direction + SP +
                vtDist + SP +
                tdDist + SP +
                frame;
    }

    public static String getLogHeader() {
        return "task" + SP +
                "direction" + SP +
                "vt_dist" + SP +
                "td_dist" + SP +
                "frame";
    }
}
