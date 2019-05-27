package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Provide a {@link Iterable} view of the {@link Entry entries} of a map. This class is null safe, meaning that if the provided
 * map is null, the iterable will simply be empty.
 *
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 */
public class MapEntries<K, V> implements Iterable<Entry<K, V>>, Serializable {

    private final Map<K, V> map;

    public MapEntries(Map<K, V> map) {
        this.map = defaultIfNull(map, Collections.<K, V> emptyMap());
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }

}
