package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPeriode;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Transformasjoner relatert til perioder, gyldighetsperioder, etc.
 */
final class XMLPeriodeTransform {

    static class Sluttdato implements Transformer<XMLPeriode, LocalDate> {
        @Override
        public LocalDate transform(XMLPeriode periode) {
            DateTime tom = periode.getTom();
            return tom != null ? tom.toLocalDate() : null;
        }
    }

    private XMLPeriodeTransform() { };
}
