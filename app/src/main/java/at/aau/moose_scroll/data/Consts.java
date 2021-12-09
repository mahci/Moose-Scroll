package at.aau.moose_scroll.data;

public class Consts {

    public static class STRINGS {
        public static final String INTRO = "INTRO";
        public static final String MOOSE = "MOOSE";
        public static final String MODE = "MODE";
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
        VERTICAL, HORIZONTAL, TWOD
    }

    public enum SCROLL_MODE {
        VERTICAL(1), TWO_DIM(2);
        private final int n;
        SCROLL_MODE(int i) { n = i; }
    }

    public enum TECH {
        DRAG, RATE_BASED
    }
}
