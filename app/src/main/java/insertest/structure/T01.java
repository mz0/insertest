package insertest.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class T01 extends StoredUnit {
    public static final String TABLE_NAME = "packet_data";

    private byte[] pBytes;
    private final List<Object> fields = Arrays.asList(generatedId, pBytes);

    public T01(byte[] ba) {
        pBytes = ba;
    }

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
        return Arrays.asList("id", "packet_data");
    }

    @Override
    public List<String> getFieldTypes() {
        return Arrays.asList("bigint", "mediumblob");
    }

    @Override
    public List<Object> getFieldsValue() {
        return fields;
    }
}
