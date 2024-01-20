package insertest.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Dictionary {
    private static final List<StoredUnit> knownTypes = new ArrayList<>();
    static {
        knownTypes.add(T01.justSample());

        ensureUniqueness(T02s.justSample());
        knownTypes.add(T02s.justSample());

        ensureUniqueness(T03j.justSample());
        knownTypes.add(T03j.justSample());
    }

    private static void ensureUniqueness(StoredUnit sample) {
        List<String> tableNames = knownTypes.stream().map(StoredUnit::getTableName).collect(Collectors.toList());
        if (tableNames.contains(sample.getTableName()))
            throw new IllegalArgumentException(
                    format("type %s : duplicate table name %s", sample.getClass().getSimpleName(), sample.getTableName())
            );
    }

    private Dictionary() {}

    public static List<StoredUnit> getMessageSamples() {
        if (knownTypes.isEmpty()) throw new IllegalStateException("Dictionary is empty");
        return knownTypes;
    }

    public static int numberOfTypes() {
        return knownTypes.size();
    }
}
