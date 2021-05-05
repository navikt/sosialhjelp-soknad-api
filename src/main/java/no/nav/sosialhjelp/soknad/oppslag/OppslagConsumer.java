package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.kontonummer.KontonummerDto;
import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingerResponseDto;

public interface OppslagConsumer {

    void ping();

    KontonummerDto getKontonummer(String ident);

    UtbetalingerResponseDto getUtbetalingerSiste40Dager(String ident);
}
