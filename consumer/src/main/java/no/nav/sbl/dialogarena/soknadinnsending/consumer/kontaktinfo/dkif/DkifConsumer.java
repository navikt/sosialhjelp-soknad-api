package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfoBolk;

public interface DkifConsumer {

    void ping();

    DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident);

}
