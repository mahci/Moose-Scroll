package at.aau.moose_scroll.data;

import static at.aau.moose_scroll.data.Consts.STRINGS.*;

public class Memo {
    private String type;
    private String value;

    /**
     * Constructor
     * @param tp Type
     * @param vl Value
     */
    public Memo(String tp, String vl) {
        type = tp;
        value = vl;
    }

    @Override
    public String toString() {
        return type + SP + value;
    }
}
