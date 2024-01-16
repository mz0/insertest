package insertest.structure;

import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    private static final List<StoredUnit> knownTypes = new ArrayList<>();
    static {
        byte[] e = {};
        knownTypes.add(new T01(e));
    };
    private Dictionary() {};

    private Dictionary instance = new Dictionary();

    public static List<StoredUnit> getMessageSamples() {
        if (knownTypes.isEmpty()) throw new IllegalStateException("Dictionary is empty");
        return knownTypes;
    }

}
