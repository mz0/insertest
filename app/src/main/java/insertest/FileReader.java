package insertest;

import insertest.structure.StoredUnit;
import insertest.structure.T01;
import insertest.structure.T02s;
import insertest.structure.T03j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class FileReader implements AutoCloseable, Iterator<StoredUnit> {
    private final static Logger log = LogManager.getLogger();
    private boolean isClosed;
    private int mCount = 0;
    private final BufferedReader lineReader;

    private final String fileName;

    private final LinkedList<StoredUnit> compound = new LinkedList<>();

    private final AtomicInteger idGen;

    private final byte[] sillyBytes = new byte[]{1, 2, 3, -127};

    public FileReader(Path file, AtomicInteger startId) throws IOException {
        this.idGen = startId; log.info("current id={}", idGen.get());
        if (file.toFile().isDirectory()) throw new IllegalArgumentException(file + "is a directory");
        fileName = file.getFileName().toString();
        lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile())));
        isClosed = false;
    }

    private boolean fake(String fileLine) throws IOException {
        if (!compound.isEmpty()) throw new RemoteException("attempt to fake messages while compound isn't empty");
        String[] params = fileLine.split("\\s+");
        if (params.length < 3) dieOn(fileLine);
        List<Integer> pN = Arrays.stream(params)
            .map(s -> { int n = Integer.parseInt(s);
                if (n < 0) dieOn(fileLine);
                return n; })
            .collect(Collectors.toList());
        // pktLen (T01) - T02 count - T03 count
        int c02 = pN.get(1); // counts of T02 and T03 messages respectively
        int c03 = pN.get(2);
        int currentPacket = mCount++;
        compound.add(new T01(sillyBytes, format("%s-packet-%02d", fileName, currentPacket)));
        compound.addAll(tempList(currentPacket, c02, c03));
        fillIds(compound);
        return !compound.isEmpty();
    }

    private void fillIds(LinkedList<StoredUnit> compound) {
        compound.forEach(su -> su.setGeneratedId(idGen.getAndIncrement()));
    }

    private ArrayList<StoredUnit> tempList(int currentPacket, int count02, int count03) {
        ArrayList<StoredUnit> temp = new ArrayList<>(count02 + count03);
        for (int i = 0;       i < count02; i++) {
            temp.add(new T02s(sillyBytes, format("%s-T02-%02d", fileName, currentPacket)));
        }
        for (int i = count02; i < count02 + count03; i++) {
            temp.add(new T03j(sillyBytes, format("%s-T03-%02d", fileName, currentPacket)));
        }
        Collections.shuffle(temp);
        int n02 = 0; int n03 = 0;
        for (StoredUnit u : temp) {
            if (u instanceof T02s) { n02++;  u.appendNote(n02); }
            if (u instanceof T03j) { n03++;  u.appendNote(n03); }
        }
        return temp;
    }

    private void dieOn(String badLine) {
        String errMsg = "bad line '" + badLine + "' in file " + fileName;
        throw new RuntimeException(errMsg);
    }

    @Override
    public boolean hasNext() {
        if (compound.isEmpty()) {
            try {
                return tryNextLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private boolean tryNextLine() throws IOException {
        String nLine;
        do {
            nLine = lineReader.readLine();
        } while (nLine != null && nLine.startsWith("#"));
        return nLine != null && fake(nLine);
    }

    @Override
    public StoredUnit next() {
        return compound.removeFirst();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void dieIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("FileReader is closed");
        }
    }

    @Override
    public void close() throws Exception {
        if (!isClosed) {
            isClosed = true;
            lineReader.close();
        }
    }
}
