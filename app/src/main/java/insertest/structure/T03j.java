package insertest.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class T03j extends StoredUnit {
    public static final String TABLE_NAME = "jm";
    public static final T03j aSample;

    static {
        byte[] empty = {};
        aSample = new T03j(empty, null);
    }

    public static T03j justSample() {
        return aSample;
    }

    private final byte[] pBytes;

    public T03j(byte[] ba, String note) { pBytes = ba; this.note = note; }

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
