package insertest.structure;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public abstract class StoredUnit {
    protected String note;
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

    public void appendNote(int n) {
        note = note + format("-%03d", n);
    }

    /**
     * A note looks like {@code "file02-T02-01-002"} (17 chars) where</br>
     * "file02" is the source file name,/br>
     * T02 - type id,
     * -01- - ordinal of the "Ethernet packet" which contains this "message"/br>
     * -002 - ordinal of this message (inside the containing packet)
     * @return
     */
    public Object getNote() {
        return note;
    }
}
