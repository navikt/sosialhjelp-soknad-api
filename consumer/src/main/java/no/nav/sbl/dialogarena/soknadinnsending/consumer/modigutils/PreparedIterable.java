package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.Mappable;
import no.nav.modig.lang.MayBeEmpty;
import no.nav.modig.lang.collections.iter.CollectableIterable;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Decorates another Iterable to make it subject to various operations to adapt how elements are retrieved.
 *
 * @param <T>
 *            The type of elements this iterable yields.
 *
 * @see #filter(Predicate)
 * @see #map(Transformer)
 * @see #take(int)
 * @see #indexed()
 * @see #indexedFrom(int)
 */
public class PreparedIterable<T> extends CollectableIterable<T> implements Mappable<T>, MayBeEmpty {

    private final Iterable<T> iterable;

    public PreparedIterable(Iterable<T> iterable) {
        this.iterable = defaultIfNull(iterable, Collections.<T> emptyList());
    }

    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }

    /**
     * Create a filtered Iterable which only returns the elements which passes the given predicate.
     *
     * @param predicate
     *            the filter predicate
     * @return the new Iterable
     */
    public final PreparedIterable<T> filter(final Predicate<? super T> predicate) {
        return !isEmpty() ? new PreparedIterable<T>(new FilteredIterable<T>(this, predicate)) : this;
    }

    /**
     * Create a "bounded" Iterable, i.e. only maximum <code>amount</code> first elements will be returned from the Iterable.
     *
     * <p>
     * Examples
     * </p>
     *
     * <pre>
     *   prepare({@link java.util.Arrays#asList(Object...) asList('a','b','c')}).take(2) &#8773; asList('a','b')
     *   prepare(asList('a','b','c')).take(4) &#8773; asList('a','b','c')
     * </pre>
     *
     * @param amount
     *            The maximum amount of elements.
     * @return the new Iterable
     */
    public final PreparedIterable<T> take(int amount) {
        return !isEmpty() ? new PreparedIterable<T>(new BoundedIterable<T>(this, amount)) : this;
    }

    /**
     * Make each element be "mapped" to another type using a mapping function (i.e. a {@link Transformer}) before it is returned
     * from the Iterable. A mapping may for instance extract a property from an object, convert numerical values to Strings, or
     * any other transforming.
     *
     * @param transformer
     *            The mapping function
     * @return the new Iterable, which will be of the same type as the output type of the mapping function.
     *
     * @see TransformerUtils#asString()
     * @see TransformerUtils#trimmed()
     * @see TransformerUtils#toLowerCase()
     */
    @Override
    public final <O> PreparedIterable<O> map(Transformer<? super T, O> transformer) {
        return !isEmpty() ? new PreparedIterable<O>(new TransformingIterable<T, O>(this, transformer)) : (PreparedIterable<O>) this;
    }

    /**
     * @param transformer The mapping function, must produce Iterables of O
     * @return an Iterable resulting from mapping 'transformer' across this iterable and appending all the results
     */
    public <O> PreparedIterable<O> flatmap(Transformer<? super T, ? extends Iterable<O>> transformer) {
        return new PreparedIterable<O>(new FlatmappingIterable<T, O>(this, transformer));
    }

    /**
     * Returns the elements of the Iterable together with its iteration index, where the first element has index 0.
     *
     * @return the new Iterable of type {@link Elem}.
     */
    public final PreparedIterable<Elem<T>> indexed() {
        return this.indexedFrom(0);
    }

    /**
     * Returns the elements of the Iterable together with its iteration index, starting from a given index.
     *
     * @param startIndex
     *            the index to assign to the first element
     * @return the new Iterable of type {@link Elem}.
     */
    public final PreparedIterable<Elem<T>> indexedFrom(int startIndex) {
        return !isEmpty() ? new PreparedIterable<Elem<T>>(new IndexingIterable<T>(startIndex, this)) : (PreparedIterable<Elem<T>>) this;
    }

    /**
     * Append elements to the end of this iterable.
     *
     * @param anotherIterable
     * @return A <em>new</em> iterable with <code>anotherIterable</code> appended to the end.
     */
    public final PreparedIterable<T> append(Iterable<? extends T> anotherIterable) {
        ChainedIterable<T> chain = new ChainedIterable<T>(this.iterable, anotherIterable);
        return chain.iterator().hasNext() ? new PreparedIterable<T>(chain) : this;
    }

    /**
     * @see #append(Iterable)
     */
    public final <E extends T> PreparedIterable<T> append(E... moreElements) {
        ChainedIterable<T> chain = new ChainedIterable<T>(this.iterable, moreElements != null ? asList(moreElements) : Collections.<T> emptyList());
        return chain.iterator().hasNext() ? new PreparedIterable<T>(chain) : this;
    }

    /**
     * Prepend elements before the elements of this iterable.
     *
     * @param anotherIterable
     * @return A <em>new</em> iterable with <code>anotherIterable</code> prepended at the beginning.
     */
    public final PreparedIterable<T> prepend(Iterable<? extends T> anotherIterable) {
        ChainedIterable<T> chain = new ChainedIterable<T>(anotherIterable, this.iterable);
        return chain.iterator().hasNext() ? new PreparedIterable<T>(chain) : this;
    }

    /**
     * @see #prepend(Iterable)
     */
    public final <E extends T> PreparedIterable<T> prepend(E... moreElements) {
        ChainedIterable<T> chain = new ChainedIterable<T>(moreElements != null ? asList(moreElements) : Collections.<T> emptyList(), this.iterable);
        return chain.iterator().hasNext() ? new PreparedIterable<T>(chain) : this;
    }


    /**
     * @return <code>true</code> if there is no elements in this iterable, <code>false</code> otherwise.
     */
    @Override
    public final boolean isEmpty() {
        return !iterator().hasNext();
    }


    /**
     * Skips the first element and returns the remaining elements, or an empty iterable
     * if the head element is the only one. If this iterable is empty, <code>tail()</code>
     * throws {@link NoSuchElementException}.
     *
     * @return The elements after the first (head) element.
     */
    public final PreparedIterable<T> tail() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return new PreparedIterable<T>(new SkipLeadingElements<T>(1, this));
        }
    }

    /**
     * Reduces the iterable to a single value using fn, initial value null
     */
    public final <U> U reduce(ReduceFunction<? super T, U> fn) {
        return reduce(fn, null);
    }

    /**
     * Reduces the iterable to a single value using fn, using the given initial value
     */
    public final <U> U reduce(ReduceFunction<? super T, U> fn, U initial) {
        U current = initial == null ? fn.identity() : initial;
        for (T value : this) {
            current = fn.reduce(current, value);
        }
        return current;
    }


}
