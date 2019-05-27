package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Transformer;

class GetValue<V> implements Transformer<Optional<V>, V> {

    @Override
    public V transform(Optional<V> input) {
        return input.get();
    }

}
