package inserts;

import inserts.pipeline.ConnectOpts;
import inserts.pipeline.Writer;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static inserts.pipeline.ConnectOpts.MYSQL_DEFAULT_URL;

public class App {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        Logger log = LogManager.getLogger();
        var connectOpts = getDbConnectParameters(log);
        log.info("DB user={}, jdbcUrl={}", connectOpts.username, connectOpts.jdbcUrl);
        final int modulo = 99;
        final int asyncWrites = 22; // we have 100 independent tables, which hypothetically may be written in parallel

        var writer = new Writer(connectOpts, asyncWrites);
        int numberOfTables = modulo + 1;
        for (int i = 0; i < numberOfTables; i++) {
            String tableName = "res" + i;
            var tableOk = writer.ensureSimpleTable(tableName).get(1000, TimeUnit.MILLISECONDS);
            if (tableOk) {
                log.debug("{} simple table OK", tableName);
            }
        }
        log.info("{} simple tables OK", numberOfTables);

        Flux.range(0, 160).groupBy(i -> i % modulo)
                .flatMap(groupedFlux -> {log.info("grpFlux {}", groupedFlux.key());
                         return groupedFlux.buffer(3)
                        .flatMap(list -> {
                            log.info("{} mod{} list.size={}", groupedFlux.key(), modulo, list.size());
                            Mono<Object> asyncOpResult = Mono.defer(() -> Mono.fromCompletionStage(
                                    writer.longOp(groupedFlux.key(), list))
                            );
                            return Flux.concatDelayError(asyncOpResult);
                        }, /* concurrency */ 1);
                }, /* concurrency */ modulo)
                .doOnNext(r -> log.info("done {}", r))
                .doOnError(error -> log.error("{}", error))
                .doOnComplete(() -> log.info("Pipeline complete"))
                .subscribe();

        Future<Void> closeResult = writer.close();
        if (closeResult.isComplete()) log.info("AsyncWriter closed");
        log.info("Finished");
    }

    static ConnectOpts getDbConnectParameters(Logger log) {
        Path propFile = Paths.get("db.properties");
        String url, user, password;
		try (FileInputStream fis = new FileInputStream(propFile.toFile())) {
            var props = new Properties();
            props.load(fis);
            url = props.getProperty("db.url", MYSQL_DEFAULT_URL);
            user = props.getProperty("db.user", "shsha");
            password = props.getProperty("db.password", "999");
        } catch (FileNotFoundException e) {
            log.warn("{} not found. Loading default JDBC URL, user, password", propFile.toAbsolutePath());
            url = MYSQL_DEFAULT_URL;
            user = "shsha";
            password = "999";
        } catch (IOException e) {
            throw new RuntimeException("error reading " + propFile.toAbsolutePath());
        }
        return new ConnectOpts(url, user, password);
    }
}
