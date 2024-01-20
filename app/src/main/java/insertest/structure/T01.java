package insertest.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class T01 extends StoredUnit {
    public static final String TABLE_NAME = "pd";
    public static final T01 aSample;

    static {
        byte[] empty = {};
        aSample = new T01(empty);
    }

    public static T01 justSample() {
        return aSample;
    }

    private final byte[] pBytes;

    public T01(byte[] ba) { pBytes = ba; note = null; }
    public T01(byte[] ba, String note) { pBytes = ba; this.note = note; }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Map<String, TypeHint> getFieldTypeHints() {
        return null;
    }

    @Override
    public List<String> getFieldsNames() {
        return Arrays.asList("id", "note", "packet_data");
    }

    @Override
    public List<String> getFieldTypes() {
        return Arrays.asList("bigint", "varchar(63)", "mediumblob");
    }

    @Override
    public List<Object> getFieldsValue() {
        return Arrays.asList(generatedId, note, pBytes);
    }
}
