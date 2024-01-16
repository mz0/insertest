package insertest;

import insertest.structure.StoredUnit;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StatementMaker {

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";

    private static final String CREATE_TABLE_TEMPLATE = "CREATE TABLE %s (%s PRIMARY KEY(id))";

    private static final String INSERT = "INSERT INTO %s VALUES %s";
    private static final String INSERT_TUPLE = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final int TUPLE_SIZE = 17;

    public static String dropTable(StoredUnit sample) {
        return String.format(DROP_TABLE, sample.getTableName());
    }

    public static String createTable(StoredUnit sample) {
        String columns = "";
        List<String> fields = sample.getFieldsNames();
        List<String> types = sample.getFieldTypes();
        for (int i = 0; i < fields.size(); i++) {
            columns = columns + fields.get(i) + " " + types.get(i) + ",";
        }
        return String.format(CREATE_TABLE_TEMPLATE, sample.getTableName(), columns);
    }

    public static String insertBatch(Collection<StoredUnit> sus) {
        int batchSize = sus.size();
        String insertTuple = IntStream.range(0, batchSize)
                .mapToObj(it -> INSERT_TUPLE)
                .collect(Collectors.joining(","));
        List<Object> insertValues = IntStream.range(0, TUPLE_SIZE - 1)
                .mapToObj(it -> RandomStringUtils.randomAlphanumeric(60))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        String table = sus.stream().findAny().get().getTableName();
        return String.format(INSERT, table, insertTuple);
    }
}
