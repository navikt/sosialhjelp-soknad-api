package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Predicate;

class IsSome<V> implements Predicate<Optional<V>> {

    @Override
    public boolean evaluate(Optional<V> optional) {
        return optional.isSome();
    }

}
