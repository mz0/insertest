package inserts.pipeline;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.vertx.core.Vertx.vertx;
import static java.util.Objects.requireNonNull;

public class Writer {
    private final static Logger log = LogManager.getLogger();

    private final Pool pool;
    private final Vertx vtx = vertx();

    public Writer(ConnectOpts connOpts, int asyncWriterPoolMax) {
        requireNonNull(connOpts, "ConnectOptions");
        JsonObject poolCfg = new JsonObject()
                .put("provider_class", io.vertx.ext.jdbc.spi.impl.C3P0DataSourceProvider.class.getName())
                .put("datasourceName", "asyncDB")
                .put("driver_class", com.mysql.cj.jdbc.Driver.class.getName())
                .put("url", connOpts.jdbcUrl)
                .put("user", connOpts.username)
                .put("password", connOpts.password)
                .put("max_idle_time", 30) // seconds
                .put("initial_pool_size", 8)
                .put("max_pool_size", asyncWriterPoolMax);

        this.pool = JDBCPool.pool(vtx, poolCfg);
    }

    public CompletableFuture<List<Integer>> longOp(int remainder, List<Integer> batch) {
        String tableName = "res" + remainder;
        List<String> fields = Collections.singletonList("value");
        var perRowPlaceHolders = fields.stream().map(f -> "?").collect(Collectors.joining(",", "(", ")"));
        var allPlaceHolders = batch.stream().map(r -> perRowPlaceHolders).collect(Collectors.joining(","));

        var insertTemplate = "INSERT INTO %s (%s) VALUES %s";
        var columns = String.join(",", fields);
        var query = String.format(insertTemplate, tableName, columns, allPlaceHolders);

        var allValues = asTuple(batch);
        return pool.withTransaction(conn -> conn
                    .preparedQuery(query)
                    .execute(allValues).compose(r -> Future.succeededFuture(batch), Future::failedFuture)
                ).toCompletionStage().toCompletableFuture();
    }

    private Tuple asTuple(List<Integer> batch) {
        var result = Tuple.tuple();
        for (Integer su : batch) {
            result.addValue(su);
        }
        return result;
    }

    public CompletableFuture<Boolean> ensureSimpleTable(String tableName) { // FIXME
        return doEnsureTable(tableName).map(ar -> true)
                    .toCompletionStage().toCompletableFuture();
    }

    private Future<RowSet<Row>> doEnsureTable(String tableName) {
        String createQuery = "CREATE TABLE IF NOT EXISTS %s ("
                + "id INT NOT NULL AUTO_INCREMENT,"
                + "value INT NOT NULL,"
                + "saved TIMESTAMP(6) DEFAULT NOW(6),"
                + "PRIMARY KEY(id))";
        return pool
                .query(String.format(createQuery, tableName))
                .execute();
    }

    public Future<Void> close() {
        pool.close();
        log.info("DB connection Pool closed");
        return vtx.close();
    }
}
