package insertest.structure;

import java.util.List;
import java.util.Map;

public abstract class StoredUnit {
    protected long generatedId;
    public long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(long generatedId) {
        this.generatedId = generatedId;
    }

    public abstract String getTableName();

    public abstract Map<String, TypeHint> getFieldTypeHints();
    public abstract List<String> getFieldsNames();
    public abstract List<String> getFieldTypes();
    public abstract List<Object> getFieldsValue();
}
