package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * A simple structure holding a value together with an index (relative to some context) for the value.
 *
 * @param <T>
 *            The value type.
 */
public final class Elem<T> implements Serializable {

    public final int index;
    public final T value;

    private String asString;

    public static <T> Elem<T> elem(int index, T value) {
        return new Elem<T>(index, value);
    }

    private Elem(int index, T value) {
        this.index = index;
        this.value = value;
        this.asString = index + "=>" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Elem) {
            Elem<?> another = (Elem<?>) object;
            return new EqualsBuilder().append(index, another.index).append(value, another.value).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(index).append(value).toHashCode();
    }

    @Override
    public String toString() {
        return asString;
    }

    public static <V> Transformer<Elem<V>, V> value() {
        return new Transformer<Elem<V>, V>() {
            @Override
            public V transform(Elem<V> elem) {
                return elem.value;
            }
        };
    }

    public static <V> Transformer<Elem<V>, Integer> index() {
        return new Transformer<Elem<V>, Integer>() {
            @Override
            public Integer transform(Elem<V> elem) {
                return elem.index;
            }
        };
    }

}
