package at.aau.moose_scroll.data;

public class Consts {

    public static class STRINGS {
        public static final String SP = ";";
        public static final String INTRO = "INTRO";
        public static final String MOOSE = "MOOSE";
        public final static String TECHNIQUE = "TECHNIQUE";
        public static final String SCROLL = "SCROLL";
        public static final String DRAG = "DRAG";
        public static final String RB = "RABA";
        public static final String STOP = "STOP";
        public final static String CONFIG = "CONFIG";
        public final static String SENSITIVITY = "SENSITIVITY";
        public final static String GAIN = "GAIN";
        public final static String DENOM = "DENOM";
        public final static String EMPTY = "";
    }

    public static class INTS {
        public static final int CLOSE_DLG = 0;
        public static final int SHOW_DLG = 1;
    }

    public enum MODE {
        VERTICAL, HORIZONTAL, TWOD
    }

    public enum SCROLL_MODE {
        VERTICAL(1), TWO_DIM(2);
        private final int n;
        SCROLL_MODE(int i) { n = i; }
    }

    public enum TECHNIQUE {
        DRAG(1), RATE_BASED(2), FLICK(3), MOUSE(4);
        private final int n;
        TECHNIQUE(int i) { n = i; }
    }
}
