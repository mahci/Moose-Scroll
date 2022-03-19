package at.aau.moose_scroll.experiment;

import at.aau.moose_scroll.tools.Utils;

public class Experiment {

    public enum TASK {
        VERTICAL, TWO_DIM;
        private static final TASK[] values = values();

        public static TASK get(int ord) {
            if (ord < values.length) return values[ord];
            else return values[0];
        }
    }

    public enum TECHNIQUE {
        DRAG, RATE_BASED, FLICK, MOUSE;
        private static final TECHNIQUE[] values = values();
        public static TECHNIQUE get(int ord) {
            if (ord < values.length) return values[ord];
            else return values[0];
        }
    }

    // Directions
    public enum DIRECTION {
        N(0), S(1), E(2), W(3), NE(4), NW(5), SE(6), SW(7);
        private final int n;

        DIRECTION(int i) {
            n = i;
        }

        // Get a NE/NW/SE/SW randomly
        public static DIRECTION randTd() {
            return DIRECTION.values()[Utils.randInt(4, 8)];

        }
        // Get a NE/SE randomly
        public static DIRECTION randOne(DIRECTION d0, DIRECTION d1) {
            if (Utils.randInt(0, 2) == 0) return d0;
            else return d1;
        }
    }
}
