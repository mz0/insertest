package insertest;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Insert {

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";

    public static final String CREATE_TABLE =
            "CREATE TABLE %s (" +
                    "id BIGINT PRIMARY KEY," +
                    "column_a VARCHAR(60)," + // 1
                    "column_b VARCHAR(60)," +
                    "column_c VARCHAR(60)," +
                    "column_d VARCHAR(60)," +
                    "column_e VARCHAR(60)," +
                    "column_f VARCHAR(60)," +
                    "column_g VARCHAR(60)," +
                    "column_h VARCHAR(60)," +
                    "column_i VARCHAR(60)," +
                    "column_j VARCHAR(60)," +
                    "column_k VARCHAR(60)," +
                    "column_l VARCHAR(60)," +
                    "column_m VARCHAR(60)," +
                    "column_n VARCHAR(60)," +
                    "column_o VARCHAR(60)," +
                    "column_p VARCHAR(60)" +  // 16
                    ")";

    private static final String INSERT = "INSERT INTO %s VALUES %s";
    private static final String INSERT_TUPLE = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final int TUPLE_SIZE = 17;

    public final String dropTable;
    public  final String createTable;
    public final String insert;
    public final List<Object> insertValues;


    public Insert(String table, int batchSize) {
        this.dropTable = String.format(DROP_TABLE, table);
        this.createTable = String.format(CREATE_TABLE, table);

        String insertTuple = IntStream.range(0, batchSize)
                .mapToObj(it -> INSERT_TUPLE)
                .collect(Collectors.joining(","));
        this.insert = String.format(INSERT, table, insertTuple);
        this.insertValues = IntStream.range(0, TUPLE_SIZE - 1)
                .mapToObj(it -> RandomStringUtils.randomAlphanumeric(60))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
