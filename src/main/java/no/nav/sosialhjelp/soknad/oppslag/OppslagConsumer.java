package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingerResponseDto;

public interface OppslagConsumer {

    void ping();

    UtbetalingerResponseDto getUtbetalingerSiste40Dager(String ident);
}
