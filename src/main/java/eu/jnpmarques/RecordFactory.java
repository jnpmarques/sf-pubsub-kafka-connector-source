package eu.jnpmarques;

import java.util.Map;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.OffsetStorageReader;

public interface RecordFactory<V, O> {
    public SourceRecord createSourceRecord(DataPoint<V, O> dataPoint, String topic);

    /**
     * 
     * @param offsetStorageReader
     * @return a map with the current offset for each partition
     */
    public Map<String, O> getOffsetMap(OffsetStorageReader offsetStorageReader, String topic);

    public void stop();
}
