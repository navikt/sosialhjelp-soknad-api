package no.nav.sosialhjelp.soknad.consumer.dkif;

import no.nav.sosialhjelp.soknad.consumer.dkif.dto.DigitalKontaktinfoBolk;

public interface DkifConsumer {

    void ping();

    DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident);

}
