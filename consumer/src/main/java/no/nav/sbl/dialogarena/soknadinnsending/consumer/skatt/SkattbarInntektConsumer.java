package no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt;


import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.SkattbarInntekt;

public interface SkattbarInntektConsumer {

    SkattbarInntekt hentSkattbarInntekt(String fnummer);

    void ping();
}
