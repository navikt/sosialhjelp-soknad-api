package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.MayBeEmpty;
import no.nav.modig.lang.option.Optional;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.option.Optional.optional;
import static org.apache.commons.collections15.CollectionUtils.forAllDo;
import static org.apache.commons.collections15.TransformerUtils.stringValueTransformer;

public abstract class CollectableIterable<T> implements Iterable<T>, Serializable, MayBeEmpty {

    public List<T> collect() {
        return unmodifiableList(collectIn(new ArrayList<T>()));
    }

    public List<T> collect(Comparator<? super T> comparator) {
        List<T> collected = collectIn(new ArrayList<T>());
        sort(collected, comparator);
        return collected;
    }

    public <C extends Collection<? super T>> C collectIn(C collection) {
        for (T element : this) {
            collection.add(element);
        }
        return collection;
    }

    public T[] collectIn(T[] array) {
        return collect().toArray(array);
    }



    /**
     * @return the first element of this iterable, if it exist.
     */
    public Optional<T> head() {
        Iterator<T> iterator = iterator();
        return iterator.hasNext() ? optional(iterator.next()) : Optional.<T> none();
    }



    /**
     * Check if an element exists.
     *
     * @param element the element to search for
     * @return true if <code>element</code> exists, false otherwise.
     */
    public boolean exists(T element) {
        return exists(equalTo(element));
    }

    /**
     * Check if an element exists.
     *
     * @param predicate The predicate which matches the object.
     * @return true if <code>predicate</code> evaluates to true for at least one of the elements,
     *         false otherwise.
     */
    public boolean exists(Predicate<? super T> predicate) {
        return CollectionUtils.exists(this, predicate);
    }

    /**
     * @return true if this iterable does not contain any elements, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Perform an operation for each element in this iterable.
     *
     * @param operation The operation to perform.
     */
    public void forEach(Closure<? super T> operation) {
        forAllDo(this, operation);
    }

    /**
     * Textual representation of the elements in this Iterable. Calling this method will do the actual iteration through all the
     * elements, get their string representation and give the same <code>toString()</code> as a {@link List list} of strings.
     * Typically, the format will look like this: <code>[elem1, elem2, elem3]</code>.
     * <p>
     * You would not normally use the <code>toString()</code> of an iterable in production code, but it is overridden here for
     * convenience for test purposes.
     * </p>
     *
     * @return String representation of all the elements in this Iterable.
     */
    @Override
    public String toString() {
        return CollectionUtils.collect(iterator(), stringValueTransformer()).toString();
    }

}
