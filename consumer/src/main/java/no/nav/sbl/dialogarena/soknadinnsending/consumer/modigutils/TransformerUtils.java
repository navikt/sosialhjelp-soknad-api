package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.collections.transform.AppendPathname;
import no.nav.modig.lang.collections.transform.Cast;
import no.nav.modig.lang.collections.transform.FieldToFieldName;
import no.nav.modig.lang.collections.transform.GetFileFromUrl;
import no.nav.modig.lang.collections.transform.JarWithClass;
import no.nav.modig.lang.collections.transform.JoinAllTransformers;
import no.nav.modig.lang.collections.transform.JoinedCollectionElements;
import no.nav.modig.lang.collections.transform.KeyOfMapEntry;
import no.nav.modig.lang.collections.transform.ListElementAt;
import no.nav.modig.lang.collections.transform.MakeDirectories;
import no.nav.modig.lang.collections.transform.NameOfEnumConstant;
import no.nav.modig.lang.collections.transform.ObjectToFieldValue;
import no.nav.modig.lang.collections.transform.StringLengthTransformer;
import no.nav.modig.lang.collections.transform.StringToClasspathResource;
import no.nav.modig.lang.collections.transform.StringToEnumConstant;
import no.nav.modig.lang.collections.transform.StringToField;
import no.nav.modig.lang.collections.transform.ToLowerCase;
import no.nav.modig.lang.collections.transform.ToUpperCase;
import no.nav.modig.lang.collections.transform.TrimString;
import no.nav.modig.lang.collections.transform.TypeOfObject;
import no.nav.modig.lang.collections.transform.ValueOfMapEntry;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static org.apache.commons.collections15.TransformerUtils.chainedTransformer;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Facade for various transformers.
 */
public final class TransformerUtils {

    /**
     * Transformer which enables chaining of several transformers.
     * <p>
     * E.g:<br/>
     * <code>Transformer&lt;X,Y&gt; -&gt; Transformer&lt;Y,Z&gt;</code> results in a <code>Transformer&lt;X,Z&gt;</code>
     * (transforms from X to Z).
     */
    public static class TransformerChainer<IN, OUT> implements Transformer<IN, OUT>, Serializable {

        public final Transformer<IN, OUT> chained;

        public TransformerChainer(Transformer<IN, OUT> firstTransformer) {
            chained = firstTransformer;
        }

        public <OUT2> TransformerChainer<IN, OUT2> then(Transformer<? super OUT, OUT2> nextTransformer) {
            Transformer<IN, OUT2> chainedTransformer = chainedTransformer(this, nextTransformer);
            return new TransformerChainer<IN, OUT2>(chainedTransformer);
        }

        @Override
        public OUT transform(IN input) {
            return chained.transform(input);
        }

        public Closure<? super IN> then(final Closure<? super OUT> closure) {
            return new Closure<IN>() {
                @Override
                public void execute(IN input) {
                    closure.execute(chained.transform(input));
                }
            };
        }

    }

    /**
     * Convenience method to run a transformer on a single input object. Reads more fluently than
     * <code>someTransformer.transform(input)</code>.
     *
     * @param <IN>
     *            type of input object
     * @param <OUT>
     *            type of output, i.e. the return type
     * @param transformer
     *            the transformer
     * @param input
     *            input object to the transformer
     * @return the result from the transformer
     */
    public static <IN, OUT> OUT get(Transformer<IN, OUT> transformer, IN input) {
        return transformer.transform(input);
    }

    /**
     * Compose several transformers into a chain where the output of each transformer is fed into the next one, and the last
     * transformer defines the output type of the resulting chain (which itself is a transformer).
     *
     * @param firstTransformer
     *            The first transformer in a chain.
     *
     * @return A transformer chain which performs the given transformer. Add more transformers to the chain by using
     *         {@link TransformerChainer#then(Transformer) .then(..)}.
     */
    public static <IN, OUT> TransformerChainer<IN, OUT> first(Transformer<IN, OUT> firstTransformer) {
        return new TransformerChainer<IN, OUT>(firstTransformer);
    }

    public static JarWithClass jarWithClass() {
        return new JarWithClass();
    }

    public static StringToField pickFieldsIn(Class<?> type, Predicate<? super Field> fieldRequirement) {
        return new StringToField(type, fieldRequirement);
    }

    public static <T> ObjectToFieldValue<T> fieldValueOf(String fieldName) {
        return new ObjectToFieldValue<T>(fieldName);
    }

    public static <T> ObjectToFieldValue<T> fieldValueOf(Field field) {
        return new ObjectToFieldValue<T>(field);
    }

    public static FieldToFieldName asFieldName() {
        return new FieldToFieldName();
    }

    public static TypeOfObject typeOfObject() {
        return new TypeOfObject();
    }

    public static ToLowerCase toLowerCase() {
        return ToLowerCase.INSTANCE;
    }

    public static ToUpperCase toUpperCase() {
        return ToUpperCase.INSTANCE;
    }

    public static TrimString trimmed() {
        return TrimString.INSTANCE;
    }

    public static <T> Transformer<T, String> asString() {
        return org.apache.commons.collections15.TransformerUtils.stringValueTransformer();
    }

    @SafeVarargs
    public static <T> Transformer<T, String> joined(Transformer<? super T, ? extends Object> ... transformers) {
        return joined("", transformers);
    }

    @SafeVarargs
    public static <T> Transformer<T, String> joined(String separator, Transformer<? super T, ? extends Object> ... transformers) {
        return joined(FactoryUtils.always(defaultString(separator)), transformers);
    }

    @SafeVarargs
    public static <T> Transformer<T, String> joined(Factory<String> separator, Transformer<? super T, ? extends Object> ... transformers) {
        return joined(separator, asList(transformers));
    }

    public static <T> Transformer<T, String> joined(Iterable<? extends Transformer<? super T, ? extends Object>> transformers) {
        return joined("", transformers);
    }

    public static <T> Transformer<T, String> joined(String separator, Iterable<? extends Transformer<? super T, ? extends Object>> transformers) {
        return joined(FactoryUtils.always(defaultString(separator)), transformers);
    }

    public static <T> Transformer<T, String> joined(Factory<String> separator, Iterable<? extends Transformer<? super T, ? extends Object>> transformers) {
        return new JoinAllTransformers<T>(separator, transformers);
    }


    public static <T, I extends Iterable<? extends T>> Transformer<I, String> joined() {
        return TransformerUtils.<T, I>joinedWith("");
    }

    public static <T, I extends Iterable<? extends T>> Transformer<I, String> joinedWith(String delimiter) {
        return new JoinedCollectionElements<I>(delimiter);
    }

    public static Transformer<String, Integer> lengthOfString() {
        return StringLengthTransformer.INSTANCE;
    }

    public static <OUT> Transformer<Object, OUT> castTo(Class<OUT> targetClass) {
        return new Cast<OUT>(targetClass);
    }

    public static <T> Transformer<List<T>, T> elementAt(final int index) {
        return new ListElementAt<T>(index);
    }

    public static <E extends Enum<E>> Transformer<String, E> asEnum(Class<E> enumType) {
        return new StringToEnumConstant<E>(enumType);
    }

    public static Transformer<Enum<?>, String> enumName() {
        return NameOfEnumConstant.INSTANCE;
    }

    public static <TARGETENUM extends Enum<TARGETENUM>> Transformer<Enum<?>, TARGETENUM> enumToEnumByName(Class<TARGETENUM> enumType) {
        return first(enumName()).then(asEnum(enumType));
    }

    /**
     * Resolves a classpath resource name, always <em>from root</em> of the classpath, and returns its URL.
     *
     * @return The URL of the resolved classpath resource, or <code>null</code> if the resource does not exist.
     */
    public static Transformer<String, URL> asClasspathResourceUrl() {
        return StringToClasspathResource.INSTANCE;
    }

    /**
     * Equivalent of the {@link URL#getFile()} wrapped as a {@link File} object. In addition, the URL is
     * {@link URLDecoder#decode(String, String) decoded} using UTF-8.
     *
     * @return The File object, which may exist or not.
     */
    public static Transformer<URL, File> urlAsFile() {
        return GetFileFromUrl.INSTANCE;
    }

    public static Transformer<String, File> asFileOnClasspath() {
        return first(asClasspathResourceUrl()).then(urlAsFile());
    }

    /**
     * Creates a new {@link File} from existing files as parent of
     * the new file, and the given pathname as child, i.e. appends
     * the pathname to an existing File.
     *
     * @param pathname The pathname to append, may be a file or directory name.
     * @return the transformer.
     */
    public static Transformer<File, File> appendPathname(String pathname) {
        return new AppendPathname(pathname);
    }

    /**
     * A transformer which takes a {@link File}, tries to make its abstract
     * pathname as a directory, and then returns the same file. Obviously
     * a "function" which only performs side-effects. The call to transform
     * returns only if all necessary directories could be created (or already exists),
     * and afterwards, the directory does indeed exist ({@link File#isDirectory()}).
     * Otherwise, it throws an exception.
     *
     * @return the transformer.
     */
    public static final Transformer<File, File> makeDirs() {
        return MakeDirectories.INSTANCE;
    }

    /**
     * @return Transformer which retrieves the key of a map entries.
     */
    public static <T> Transformer<Entry<T, ?>, T> key() {
        return new KeyOfMapEntry<T>();
    }

    /**
     * @return Transformer which retrieves the value of map entries.
     */
    public static <T> Transformer<Entry<?, T>, T> value() {
        return new ValueOfMapEntry<T>();
    }

    private TransformerUtils() {
    }

    static {
        new TransformerUtils();
    }

}
