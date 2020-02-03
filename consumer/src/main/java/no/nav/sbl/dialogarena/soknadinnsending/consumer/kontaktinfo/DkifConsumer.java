package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dto.DigitalKontaktinfoBolk;

public interface DkifConsumer {

    void ping();

    DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident);

}
