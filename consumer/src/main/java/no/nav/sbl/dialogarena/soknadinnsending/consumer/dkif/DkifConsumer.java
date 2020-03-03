package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfoBolk;

public interface DkifConsumer {

    void ping();

    DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident);

}
