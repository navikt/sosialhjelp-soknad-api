package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.collections.predicate.InstanceOf;

import no.nav.modig.lang.collections.predicate.AndPredicateBuilder;
import no.nav.modig.lang.collections.predicate.AnyPredicate;
import no.nav.modig.lang.collections.predicate.BlankString;
import no.nav.modig.lang.collections.predicate.CollectionContains;
import no.nav.modig.lang.collections.predicate.CollectionSize;
import no.nav.modig.lang.collections.predicate.ContainedIn;
import no.nav.modig.lang.collections.predicate.ContainsSubstring;
import no.nav.modig.lang.collections.predicate.EmptyCollection;
import no.nav.modig.lang.collections.predicate.EqualToIgnoreCase;
import no.nav.modig.lang.collections.predicate.ExistsIn;
import no.nav.modig.lang.collections.predicate.FileExists;
import no.nav.modig.lang.collections.predicate.GreaterThanPredicate;
import no.nav.modig.lang.collections.predicate.IsStatic;
import no.nav.modig.lang.collections.predicate.LessThanPredicate;
import no.nav.modig.lang.collections.predicate.MemberWithModifier;
import no.nav.modig.lang.collections.predicate.NumericOrSpacePredicate;
import no.nav.modig.lang.collections.predicate.NumericStringPredicate;
import no.nav.modig.lang.collections.predicate.TransformerOutputPredicate;
import no.nav.modig.lang.collections.predicate.WithAnnotation;
import no.nav.modig.lang.collections.predicate.WithMemberNameIn;
import no.nav.modig.lang.collections.predicate.WithParameterTypes;
import no.nav.modig.lang.collections.predicate.WithType;
import no.nav.modig.lang.reflect.Reflect;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.TransformerUtils.asFieldName;
import static no.nav.modig.lang.collections.TransformerUtils.lengthOfString;
import static org.apache.commons.collections15.CollectionUtils.collect;
import static org.apache.commons.collections15.PredicateUtils.equalPredicate;
import static org.apache.commons.collections15.PredicateUtils.notPredicate;

/**
 * Facade for various predicate functions.
 */
public final class PredicateUtils {

    public static Predicate<Member> withName(String name) {
        return withName(equalTo(name));
    }

    public static Predicate<Member> withName(Predicate<? super String> namePredicate) {
        return new WithMemberNameIn(singleton(namePredicate));
    }

    public static Predicate<Member> withNameIn(Collection<String> candidates) {
        return new WithMemberNameIn(collect(candidates, new Transformer<String, Predicate<String>>() {
            @Override
            public Predicate<String> transform(String input) {
                return equalTo(input);
            }
        }));
    }

    /**
     * Matches classes and members which has a specified modifier.
     * @param wantedModifier The modifier to match, given as a bitmask specified
     *                       in {@link Modifier}. E.g. {@link Modifier#PUBLIC PUBLIC},
     *                       {@link Modifier#FINAL FINAL} etc.
     *
     * @return the predicate
     *
     * @see Modifier
     */
    public static Predicate<Member> withModifier(int wantedModifier) {
        return new MemberWithModifier(wantedModifier);
    }

    public static Predicate<AnnotatedElement> withAnnotation(Class<? extends Annotation> annotation) {
        return new WithAnnotation(annotation);
    }

    public static Predicate<Method> withParameterTypes(Class<?>... types) {
        return new WithParameterTypes(types);
    }

    public static Predicate<? super Field> withType(final Class<?> type) {
        return new WithType(type);
    }

    public static Predicate<String> isFieldNameIn(final Class<?> type) {
        return new ExistsIn<String>(on(Reflect.on(type).allFields()).map(asFieldName()));
    }

    public static Predicate<Member> isStatic() {
        return IsStatic.INSTANCE;
    }


    public static <T> Predicate<T> equalTo(T value) {
        return equalPredicate(value);
    }

    public static <T extends Comparable<T>> Predicate<T> lessThan(T value) {
        return new LessThanPredicate<T>(value);
    }

    public static <T extends Comparable<T>> Predicate<T> equalOrLessThan(T value) {
        return either(equalTo(value)).or(lessThan(value));
    }

    public static <T extends Comparable<T>> Predicate<T> greaterThan(T value) {
        return new GreaterThanPredicate<T>(value);
    }

    public static <T extends Comparable<T>> Predicate<T> equalOrGreaterThan(T value) {
        return either(equalTo(value)).or(greaterThan(value));
    }


    public static Predicate<String> equalToIgnoreCase(String string) {
        return new EqualToIgnoreCase(string);
    }

    public static Predicate<File> fileExists() {
        return FileExists.INSTANCE;
    }

    /**
     * @param substring
     *            the substring to search for
     * @return The Predicate equivalent to {@link String#contains(CharSequence)}
     */
    public static Predicate<String> containsString(String substring) {
        return new ContainsSubstring(substring);
    }

    /**
     * Predicate composed by applying another predicate on the result from a Transformer. Typically used evaluate a property of
     * an object.
     *
     * @param transformer
     *            the transformer to apply on the input value to the predicate
     * @param predicate
     *            the predicate to apply to the output of the transformer
     * @return The new predicate.
     */
    public static <T, C> TransformerOutputPredicate<T, C> where(Transformer<T, C> transformer, Predicate<? super C> predicate) {
        return new TransformerOutputPredicate<T, C>(transformer, predicate);
    }

    public static <T> AndPredicateBuilder<T> both(final Predicate<T> firstPredicate) {
        return new AndPredicateBuilder<T>(firstPredicate);
    }

    public static <T> AnyPredicate.Either<T> either(final Predicate<T> firstPredicate) {
        return new AnyPredicate.Either<T>(firstPredicate);
    }

    @SafeVarargs
    public static <T> Predicate<T> anyOf(Predicate<? super T>... predicates) {
        return new AnyPredicate<T>(asList(predicates));
    }

    /**
     * Convenience method of {@link #anyOf(Predicate...) anyOf(}{@link #equalTo(Object) equalTo(..)}<code>, ...)</code>
     *
     * @param <T>
     *            Type of the valid values.
     * @param validValues
     *            valid values for which at least one must exist
     * @return the created predicate
     */
    @SafeVarargs
    public static <T> Predicate<T> equalAnyOf(T... validValues) {
        return new AnyPredicate<T>(collect(asList(validValues), new AsEqualPredicate<T>()));
    }

    /**
     * Negates a predicate.
     *
     * @param <T>
     *            Type of predicate
     * @param inPredicate
     *            the predicate to negate.
     * @return the negated predicate.
     */
    public static <T> Predicate<T> not(final Predicate<T> inPredicate) {
        return notPredicate(inPredicate);
    }

    public static Predicate<String> blank() {
        return BlankString.INSTANCE;
    }

    public static Predicate<CharSequence> numeric() {
        return NumericStringPredicate.INSTANCE;
    }

    public static Predicate<CharSequence> numericOrSpace() {
        return NumericOrSpacePredicate.INSTANCE;
    }

    public static Predicate<String> withLength(int exactLength) {
        return withLength(equalTo(exactLength));
    }

    public static Predicate<String> withLength(Predicate<Integer> hasLength) {
        return where(lengthOfString(), hasLength);
    }

    public static <T extends Collection<?>> Predicate<T> empty() {
        return EmptyCollection.<T> getInstance();
    }

    public static <T extends Collection<?>> Predicate<T> withSize(int size) {
        return new CollectionSize<T>(equalTo(size));
    }

    public static <T extends Collection<?>> Predicate<T> atLeast(int minimumSize) {
        return new CollectionSize<T>(equalOrGreaterThan(minimumSize));
    }

    public static <T> Predicate<T> containedIn(Iterable<T> elements) {
        return new ContainedIn<T>(elements);
    }

    /**
     * Transforms an object into a predicate checking equivalence to that object.
     */
    private static class AsEqualPredicate<T> implements Transformer<T, Predicate<? super T>>, Serializable {
        @Override
        public Predicate<? super T> transform(T input) {
            return equalPredicate(input);
        }
    }

    /**
     * Predicate checking if a given element exists in Iterables.
     *
     * @param element the element to seek for.
     * @return the predicate
     */
    public static <E, I extends Iterable<? extends E>> Predicate<I> exists(E element) {
        return exists(equalTo(element));
    }

    /**
     * Predicate checking if there exists any element in an Iterable which satisfies a given predicate.
     *
     * @param elementPredicate The predicate to evaluate on the elements.
     * @return the predicate.
     */
    public static <E, I extends Iterable<? extends E>> Predicate<I> exists(Predicate<? super E> elementPredicate) {
        return new CollectionContains<E, I>(elementPredicate);
    }

    public static <T> Predicate<T> isA(final Class<?> cls) {
        return new InstanceOf<T>(cls);
    }

    public static <T> Predicate<T> is(final Transformer<T, Boolean> transformer) {
        return new Predicate<T>() {
            @Override
            public boolean evaluate(T t) {
                return transformer.transform(t);
            }
        };
    }

    private PredicateUtils() {
    }

    static {
        new PredicateUtils();
    }
}
