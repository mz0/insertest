package inserts;

import inserts.pipeline.ConnectOpts;
import inserts.pipeline.Writer;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static inserts.pipeline.ConnectOpts.MYSQL_DEFAULT_URL;

public class App {
    public static void main(String[] args) throws InterruptedException {
        Logger log = LogManager.getLogger();
        var connectOpts = getDbConnectParameters(log);
        log.info("DB user={}, jdbcUrl={}", connectOpts.username, connectOpts.jdbcUrl);
        final int modulo = 100;
        final int asyncWrites = 22; // we have 100 independent tables, which hypothetically may be written in parallel

        var writer = new Writer(connectOpts, asyncWrites);
        //log.info("creating 100 simple tables");
        //writer.makeSimpleTables(100); log.info("100 simple tables created OK");
        var scheduler = Schedulers.newParallel("async", asyncWrites, /* daemon */ false);
        Flux.range(0, 160).groupBy(i -> i % modulo)
                .flatMap(groupedFlux -> groupedFlux.buffer(3)
                      //.publishOn(Schedulers.newParallel("async", modulo)) // FIXME main() will not finish!
                        // daemon=true - main() does no work, =false - hangs as above
                        .map(list -> {log.info("list {}", list.size()); return list;})
                        .publishOn(scheduler)
                        .flatMap(list -> {
                            log.info("{} mod{} list.size={}", groupedFlux.key(), modulo, list.size());
                            Mono<Object> asyncOpResult = Mono.defer(() -> Mono.fromCompletionStage(
                                    writer.longOp(groupedFlux.key(), list))
                            );
                            return Flux.concatDelayError(asyncOpResult);
                        })
                )
                .subscribe(r -> log.info("done {}", r), error -> log.error("{}", error));

        log.info("Set-up complete");
        TimeUnit.MILLISECONDS.sleep(2_000); // 2 seconds wait for completion (fingers crossed)
        //scheduler.disposeGracefully();
        scheduler.dispose();
//        Future<Void> closeResult = writer.close();
//        if (closeResult.isComplete()) log.info("AsyncWriter closed");
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
