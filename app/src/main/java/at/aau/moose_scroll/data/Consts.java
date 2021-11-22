package at.aau.moose_scroll.data;

public class Consts {

    public static class STRINGS {
        public static final String INTRO = "INTRO";
        public static final String MOOSE = "MOOSE";
        public static final String SCROLL = "SCROLL";
        public static final String DRAG = "DRAG";
        public static final String RB = "RABA";
        public static final String STOP = "STOP";
        public static final String SP = "_";
    }

    public static class INTS {
        public static final int CLOSE_DLG = 0;
        public static final int SHOW_DLG = 1;
    }

    public enum MODE {
        VERTICAL, HORIZONTAL
    }

    public enum TECH {
        DRAG, RATE_BASED, MOUSE
    }
}
