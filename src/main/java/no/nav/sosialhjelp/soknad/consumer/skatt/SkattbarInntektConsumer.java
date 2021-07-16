package no.nav.sosialhjelp.soknad.consumer.skatt;


import no.nav.sosialhjelp.soknad.consumer.skatt.dto.SkattbarInntekt;

public interface SkattbarInntektConsumer {

    SkattbarInntekt hentSkattbarInntekt(String fnummer);

    void ping();
}
