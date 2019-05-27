package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.apache.commons.collections15.PredicateUtils.notNullPredicate;

public abstract class Optional<V> implements Iterable<V>, Mappable<V>, Serializable {

    private static final None<?> NONE = new None<Object>();

    @SafeVarargs
    public static <V> PreparedIterable<V> several(Optional<V>... optionals) {
        return several(asList(optionals));
    }

    public static <V> PreparedIterable<V> several(Iterable<Optional<V>> optionals) {
        return IterUtils.on(optionals).filter(new IsSome<V>()).map(new GetValue<V>());
    }

    @SuppressWarnings("unchecked")
    public static <V> Optional<V> none() {
        return (Optional<V>) NONE;
    }

    public static <V> Optional<V> optional(V value) {
        return optional(notNullPredicate(), value);
    }

    @SuppressWarnings("unchecked")
    public static <V> Optional<V> optional(Predicate<? super V> isPresent, V value) {
        if (isPresent.evaluate(value)) {
            return new Some<V>(value);
        } else {
            return (None<V>) NONE;
        }
    }

    public static final class Some<V> extends Optional<V> {

        private final V value;

        private Some(V value) {
            this.value = value;
        }

        @Override
        public Iterator<V> iterator() {
            return new ReadOnlyIterator<V>() {
                private boolean returned = false;

                @Override
                public boolean hasNext() {
                    return !returned;
                }

                @Override
                public V next() {
                    if (!returned) {
                        returned = true;
                        return value;
                    } else {
                        throw new NoSuchElementException("The value of this Optional was already obtained with this iterator. "
                                + "The next() method can only be called once given that the Optional actually contains a value.");
                    }
                }
            };
        }

        @Override
        public boolean isSome() {
            return true;
        }

        @Override
        public V getOrElse(V value) {
            return get();
        }

        @Override
        public V getOrThrow(Factory<? extends RuntimeException> e) {
            return get();
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }

        @Override
        public boolean is(Predicate<? super V> predicate) {
            return predicate.evaluate(value);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Some) {
                return new EqualsBuilder().append(this.value, ((Some<?>) other).value).isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(Some.class).append(value).toHashCode();
        }
    }

    public static final class None<V> extends Optional<V> {

        @Override
        public Iterator<V> iterator() {
            return new ReadOnlyIterator<V>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public V next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public boolean isSome() {
            return false;
        }

        @Override
        public V getOrElse(V value) {
            return value;
        }

        @Override
        public V getOrThrow(Factory<? extends RuntimeException> exceptionCreator) {
            throw exceptionCreator.create();
        }

        @Override
        public String toString() {
            return "None";
        }

        @Override
        public boolean is(Predicate<? super V> predicate) {
            return false;
        }

    }

    public V get() {
        return iterator().next();
    }

    @Override
    public final <M> Optional<M> map(Transformer<? super V, M> transformer) {
        return map(notNullPredicate(), transformer);
    }

    @SuppressWarnings("unchecked")
    public final <M> Optional<M> map(Predicate<? super M> isPresent, Transformer<? super V, M> transformer) {
        if (this instanceof None) {
            return (Optional<M>) this;
        } else {
            V value = ((Some<V>) this).get();
            return optional(isPresent, transformer.transform(value));
        }
    }

    public abstract boolean isSome();

    /**
     * Evaluate if this Optional wraps the given value/object.
     * {@link Optional#none()}{@link #is(Object) .is(V)} will <em>never</em> return <code>true</code>,
     *
     * @param value The value to evaluate if is wrapped in the Optional.
     * @return <code>true</code> if this is a {@link Some} containing the given value/object,
     *         <code>false</code> otherwise.
     */
    public boolean is(V value) {
        return is(PredicateUtils.equalTo(value));
    }



    /**
     * Evaluate if this Optional wraps a value which satisfies a given <code>Predicate</code>.
     * {@link Optional#none()}{@link #is(Object) .is(Predicate)} will <em>never</em> return <code>true</code>,
     *
     * @param predicate The predicate to evaluate on a wrapped Optional value.
     * @return <code>true</code> if this is a {@link Some} containing a value which satisfies the <code>predicate</code>,
     *         <code>false</code> otherwise.
     */
    public abstract boolean is(Predicate<? super V> predicate);

    public abstract V getOrElse(V value);


    /**
     * Shorthand method for calling {@link #getOrElse(Object) .getOrElse(null)}.
     *
     * @return The wrapped value/object, or <code>null</code> if this is a {@link None}.
     */
    public V orNull() {
        return getOrElse(null);
    }

    /**
     * Get the wrapped value or throw the given exception if it is not present.
     * <h2>Note:</h2>
     * As using this method will always require to <em>instantiate</em> the exception
     * that <em>may</em> be thrown, prefer using
     * {@link #getOrThrow(Factory) getOrThrow(Factory&lt;RuntimeException&gt;)} instead
     * to prevent unnecessary exception instantiation.
     *
     * @param e the exception to throw if no value is available in the Optional.
     * @return the wrapped value.
     */
    public V getOrThrow(RuntimeException e) {
        return getOrThrow(FactoryUtils.always(e));
    }

    /**
     * Get the wrapped value or, if it is not present, throw the exception
     * created by the given {@link Factory}.
     *
     * @param exceptionCreator provides an exception to throw if there is no wrapped value.
     * @return the wrapped value
     */
    public abstract V getOrThrow(Factory<? extends RuntimeException> exceptionCreator);

}
