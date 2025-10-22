package bdda;

import java.util.List;
import java.util.ArrayList;

public class Record {
    // Liste des valeurs (sous forme de String ou Object)
    private List<Object> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(List<Object> values) {
        this.values = values;
    }

    public List<Object> getValues() {
        return values;
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public String toString() {
        return values.toString();
    }
}
