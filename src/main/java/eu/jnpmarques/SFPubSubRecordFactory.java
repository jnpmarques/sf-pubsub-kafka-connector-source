package eu.jnpmarques;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFPubSubRecordFactory implements RecordFactory<String, String> {
    
    public static final String SOURCE_OFFSET = "replay_id";

    Logger log = LoggerFactory.getLogger(SFPubSubRecordFactory.class);

    @Override
    public SourceRecord createSourceRecord(DataPoint<String, String> dataPoint, String topic) {
        return new SourceRecord(createSourcePartition(topic),
                createSourceOffset(dataPoint.getOffset()),
                topic,
                null,
                dataPoint.getValue());
    }

    private Map<String, Object> createSourcePartition(String topic) {
        return Collections.singletonMap(topic,SOURCE_OFFSET);
    }

    private Map<String, Object> createSourceOffset(String offset) {
        return Collections.singletonMap(SOURCE_OFFSET, offset);
    }

    @Override
    public Map<String, String> getOffsetMap(OffsetStorageReader offsetStorageReader, String topic) {
        Map<String, String> offsetMap = new HashMap();
        log.debug("retrieving persisted offset for previously produced events");

        if (offsetStorageReader == null) {
            log.debug("no offset reader available");
            return offsetMap;
        }

        Map<String, Object> sourcePartition = createSourcePartition(topic);
        Map<String, Object> persistedOffsetInfo = offsetStorageReader.offset(sourcePartition);

        if (persistedOffsetInfo == null || !persistedOffsetInfo.containsKey(SOURCE_OFFSET)) {
            log.debug("no persisted offset for account ");
            return offsetMap;
        }

        String offset = (String) persistedOffsetInfo.get(SOURCE_OFFSET);
        offsetMap.put(SOURCE_OFFSET, offset);

        return offsetMap;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

}
