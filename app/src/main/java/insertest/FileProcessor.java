package insertest;

import insertest.structure.StoredUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileProcessor implements Runnable {
    private final static Logger log = LogManager.getLogger();
    private final List<Path> todo;
    private final BlockingQueue<StoredUnit> writeQueue;
    private final AtomicInteger currentId;

    public FileProcessor(List<String> toDo, BlockingQueue<StoredUnit> dbQueue, AtomicInteger startId) {
        this.currentId = startId;
        this.todo = toDo.stream().map(fn -> Paths.get(fn).toAbsolutePath()).collect(Collectors.toList());
        this.writeQueue = dbQueue;
    }

    @Override
    public void run() {
        for (Path f : todo) {
            try {
                process(f);
            } catch (IOException e) {
                log.error("error processing file {}", f.getFileName(), e);
            }
        }
    }

    private void process(Path f) throws IOException {
        try (FileReader parser = new FileReader(f, currentId)) {
            while (parser.hasNext()) {
                writeQueue.put(parser.next());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
