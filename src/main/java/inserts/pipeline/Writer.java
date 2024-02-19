package inserts.pipeline;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.vertx.core.Vertx.vertx;
import static java.util.Objects.requireNonNull;

public class Writer {
    private final static Logger log = LogManager.getLogger();

//    private final Pool pool;

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

//        this.pool = JDBCPool.pool(vertx(), poolCfg);
    }

    public CompletableFuture<Object> longOp(int remainder, List<Integer> sequence) {
        try {
            TimeUnit.MILLISECONDS.sleep(5 /*+ (15 * remainder)*/ + Math.round(5 * Math.random()));
            log.info("thread {} / rem{}: {}", Thread.currentThread().getName(), remainder, intSeq(sequence));
            return CompletableFuture.completedFuture(
                    String.format("(remainder %d) %d records OK", remainder, sequence.size()));
        } catch (InterruptedException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }
    private static String intSeq(List<Integer> seq) {
        return String.join(",", seq.stream().map(i -> i.toString()).collect(Collectors.toList()));
    }

/*
    public void createSimpleTable(String tableName) {
        String createQuery = "CREATE TABLE IF NOT EXISTS %s ("
                + "id INT NOT NULL AUTO_INCREMENT,"
                + "value INT NOT NULL,"
                + "saved TIMESTAMP(6) DEFAULT NOW(6),"
            + "PRIMARY KEY(id))";
        pool.query(String.format(createQuery, tableName))
                .execute()
                .onSuccess(re -> log.info("TABLE \"" + tableName + "\" Ok"))
                .onFailure(e -> log.error("cannot CREATE TABLE \"" + tableName + "\"", e));
    }

    public void makeSimpleTables(int num) {
        for (int i = 0; i < num; i++) {
            createSimpleTable("res" + i);
        }
    }

    public Future<Void> close() {
        return pool.close();
    }
*/
}
