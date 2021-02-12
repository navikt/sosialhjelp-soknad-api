package no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt;


import no.nav.sosialhjelp.soknad.domain.model.skattbarinntekt.SkattbarInntekt;

public interface SkattbarInntektConsumer {

    SkattbarInntekt hentSkattbarInntekt(String fnummer);

    void ping();
}
