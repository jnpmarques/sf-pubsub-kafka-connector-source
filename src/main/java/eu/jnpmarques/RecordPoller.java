package eu.jnpmarques;

import java.util.LinkedList;
import java.util.Map;

public interface RecordPoller<V, O> {

    void setConfigs(Map<String, String> configs);

    void setOffsetMaps(Map<String, O> offsetMap);

    LinkedList<DataPoint<V,O>> pollRecords();

    void stop();
}
