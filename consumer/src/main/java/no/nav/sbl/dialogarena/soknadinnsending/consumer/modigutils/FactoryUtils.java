package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.collections.factory.ClosedPredicate;
import no.nav.modig.lang.collections.factory.DisjunctionFactory;
import no.nav.modig.lang.collections.factory.HasReceivedPing;
import no.nav.modig.lang.collections.factory.NotFactory;
import no.nav.modig.lang.collections.factory.StreamHasContent;
import no.nav.modig.lang.collections.factory.TransforOutputFromFactory;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import java.io.InputStream;

public final class FactoryUtils {

    /**
     * Compose a boolean {@link Factory} which determines its created boolean based on
     * a predicate "closed on" a given parameter. This composition is typically
     * applicable only in the case when the object closed on may changes state
     * during the lifetime of the Factory.
     *
     *
     * @param parameter The parameter object to close on.
     * @param predicate The predicate to evaluate the <code>parameter</code>.
     *
     * @return true or false
     */
    public static <T> Factory<Boolean> closedOn(final T parameter, final Predicate<? super T> predicate) {
        return new ClosedPredicate<T>(parameter, predicate);
    }

    /**
     * Compose a {@link Factory} by using the output from another factory piped through a {@link Transformer}.
     *
     * @param factory The factory which creates the value.
     * @param transformer transforms the value from the factory
     * @return the composed factory.
     */
    public static <F, T> Factory<T> get(Factory<F> factory, Transformer<? super F, T> transformer) {
        return new TransforOutputFromFactory<F, T>(factory, transformer);
    }

    /**
     * Compose the disjunction (logic OR) of several {@link Factory Factory&lt;Boolean&gt;}
     *
     * @param first
     */
    public static DisjunctionFactory.Either either(Factory<Boolean> first) {
        return new DisjunctionFactory.Either(first);
    }

    /**
     * Negates the boolean from a {@link Factory Factory&lt;Boolean&gt;}
     *
     * @param factory
     */
    public static Factory<Boolean> not(Factory<Boolean> factory) {
        return new NotFactory(factory);
    }

    public static HasReceivedPing hasReceivedPingOnPort(int port) {
        HasReceivedPing pingReceiver = new HasReceivedPing(port);
        pingReceiver.newMonitor().start();
        return pingReceiver;
    }

    public static Factory<Boolean> gotKeypress() {
        return streamHasContent(System.in);
    }

    public static Factory<Boolean> streamHasContent(InputStream stream) {
        return new StreamHasContent(stream);
    }

    public static <T> Factory<T> always(T value) {
        return org.apache.commons.collections15.FactoryUtils.constantFactory(value);
    }

    private FactoryUtils() {
    }

    static {
        new FactoryUtils();
    }

}
