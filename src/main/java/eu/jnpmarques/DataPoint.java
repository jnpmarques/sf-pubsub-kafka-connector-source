package eu.jnpmarques;

public class DataPoint<V, O> {
    private V value;
    private O offset;

    DataPoint(V value, O offset) {
        this.value = value;
        this.offset = offset;
    }

    public V getValue() {
        return value;
    }

    public O getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        String format = "Value: {} , Offset: {}";
        return String.format(format, value, offset);
    }
}
