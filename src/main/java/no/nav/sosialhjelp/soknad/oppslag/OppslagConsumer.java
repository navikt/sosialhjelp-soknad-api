package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;
import no.nav.sosialhjelp.soknad.oppslag.dto.UtbetalingDto;

import java.util.List;

public interface OppslagConsumer {

    void ping();

    KontonummerDto getKontonummer(String ident);

    List<UtbetalingDto> getUtbetalingerSiste40Dager(String ident);
}
