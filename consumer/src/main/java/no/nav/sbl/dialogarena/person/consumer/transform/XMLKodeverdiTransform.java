package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLKodeverdi;
import org.apache.commons.collections15.Transformer;

/**
 * Transformere for verdier fra kodeverk.
 */
final class XMLKodeverdiTransform {

    static class Verdi implements Transformer<XMLKodeverdi, String> {
        @Override
        public String transform(XMLKodeverdi kodeverdi) {
            return kodeverdi.getValue();
        }
    }

    private XMLKodeverdiTransform() { }
}
