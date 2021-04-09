package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;

public interface OppslagConsumer {

    void ping();

    KontonummerDto getKontonummer(String ident);
}
