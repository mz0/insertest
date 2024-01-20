package insertest.pipeline;

import insertest.structure.StoredUnit;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.core.Vertx.vertx;
import static java.util.Objects.requireNonNull;

public class Writer {
    private final static Logger log = LogManager.getLogger();
    private final Pool pool;

    public Writer(JDBCConnectOptions connOpts, int asyncWriterPoolMax) {
        requireNonNull(connOpts, "JDBCConnectOptions");
                // for Agroal pool, connect timeout (ms) is passed to validationTimeout and acquisitionTimeout
        connOpts.setConnectTimeout(60 * 1_000)
                // for Agroal pool, idle timeout (minutes) is passed to reapTimeout and leakTimeout
                .setIdleTimeout(30 * 60 * 1_000);

        PoolOptions poolOptions = new PoolOptions()
                .setName("sql-client-pool")
                .setMaxSize(asyncWriterPoolMax);

        this.pool = JDBCPool.pool(vertx(), connOpts, poolOptions);
    }

    public Mono<Object> insert(List<StoredUnit> rows) {
        return Mono.defer(() -> Mono.fromCompletionStage(
        pool.withConnection(connection -> batchInsert(connection, rows))
                .toCompletionStage()));
    }

    private Future<Object> batchInsert(SqlConnection connection, List<StoredUnit> batch) {
        if (batch == null || batch.isEmpty()) {
            throw new IllegalArgumentException("null or empty list of records for INSERT");
        }
        var tableName = batch.get(0).getTableName();
        List<String> fields = batch.get(0).getFieldsNames();
        var columns = String.join(",", fields);
        log.info("got batch of {} rows for table \"{}\"", batch.size(), tableName);
        var perRowPlaceHolders = fields.stream().map(f -> "?").collect(Collectors.joining(",", "(", ")"));
        var allPlaceHolders = batch.stream().map(r -> perRowPlaceHolders).collect(Collectors.joining(","));

        var insertTemplate = "INSERT INTO %s (%s) VALUES %s";
        var query = String.format(insertTemplate, tableName, columns, allPlaceHolders); log.info("query: {}", query);

        var allValues = asTuple(batch);
        return connection.preparedQuery(query)
            .execute(allValues)
            .compose(v -> Future.succeededFuture(batch), Future::failedFuture);
    }

    private Tuple asTuple(List<StoredUnit> batch) {
        var result = Tuple.tuple();
        for (StoredUnit su : batch) {
            su.getFieldsValue().forEach(result::addValue);
        }
        return result;
    }
}
