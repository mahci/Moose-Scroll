package at.aau.moose_scroll.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private final static String NAME = "Utils/";

    /**
     * Returns a random int between the min (inclusive) and the bound (exclusive)
     * @param min   Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     * @throws IllegalArgumentException if bound < min
     */
    public static int randInt(int min, int bound) throws IllegalArgumentException {
        return ThreadLocalRandom.current().nextInt(min, bound);
    }

    /**
     * Get a random element from any int array
     *
     * @param inArray input int[] array
     * @return int element
     */
    public static int randElement(int[] inArray) {
        return inArray[randInt(0, inArray.length)];
    }

    /**
     * Generate a random permutation of {0, 1, ..., len - 1}
     *
     * @param len - length of the permutation
     * @return Random permutation
     */
    public static List<Integer> randPerm(int len) {
        String TAG = NAME + "randPerm";

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        return indexes;
    }

}
