package inserts;

import inserts.pipeline.AsyncWriter;
import inserts.pipeline.ConnectOpts;
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
        final int modulo = 100;
        // we have 100 independent tables, all of which hypothetically may be written in parallel
        final int asyncWrites = 22;

        var writer = new AsyncWriter(connectOpts, asyncWrites);
        int numberOfTables = modulo;
        for (int i = 0; i < numberOfTables; i++) {
            String tableName = writer.simpleTableName(i);
            var tableOk = writer.ensureSimpleTable(tableName).get(1000, TimeUnit.MILLISECONDS);
            if (tableOk) {
                log.debug("{} simple table OK", tableName);
            }
        }
        log.info("{} simple tables OK", numberOfTables);


        Flux.range(0, 960).doOnNext(v -> log.debug("produced {}", v)).groupBy(i -> i % modulo)
                .flatMap(groupedFlux -> {
                    log.debug("grpFlux {}", groupedFlux::key);
                    return groupedFlux
                            .buffer(4)
                            .doOnRequest(n -> log.debug("{} requested {}", groupedFlux.key(), n))
                            .flatMap(list -> {
                                log.debug("{} mod{} list.size={}", groupedFlux::key, () -> modulo, list::size);
                                return Mono.fromCompletionStage(() -> writer.write(groupedFlux.key(), list));
                            }, /* concurrency */ 1);
                }, /* concurrency */ modulo, /* prefetch */ asyncWrites)
                .doOnNext(r -> log.debug("done ({}) {}", () -> r.getClass().getName(), () -> r))
                .doOnError(error -> log.error("{}", error))
                .doOnComplete(() -> log.info("pipeline is complete"))
                .ignoreElements() // we need only "side effects" (writes to DB)
                .toFuture().get(5, TimeUnit.MINUTES);

        log.info("Pipeline has finished.");
        writer.close().get(1, TimeUnit.MINUTES);
        log.info("AsyncWriter closed OK. All done.");
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
