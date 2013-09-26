package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.feil.XMLForretningsmessigUnntak;
import org.apache.commons.collections15.Transformer;


public class XMLUnntakTransform {

    public static final class Aarsakkode implements Transformer<XMLForretningsmessigUnntak, String> {
        @Override
        public String transform(XMLForretningsmessigUnntak unntak) {
            return unntak.getFeilaarsak();
        }
    }

}
