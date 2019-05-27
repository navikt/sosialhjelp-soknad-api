package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Transformer;

/**
 * A container-type with the ability to map its containing element(s) to
 * something else.
 */
public interface Mappable<T> {

    /**
     * Applies the mappingFunction ({@link Transformer}) to each containing element,
     * if any, and returns a new container with the resulting element(s).
     *
     * @param mappingFunction The mapping, or transformation, to apply to the containing
     *                        element.
     */
    <M> Mappable<M> map(Transformer<? super T, M> mappingFunction);

}
