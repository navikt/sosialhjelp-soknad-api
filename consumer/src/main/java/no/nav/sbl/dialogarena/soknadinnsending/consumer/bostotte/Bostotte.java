package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;

import java.time.LocalDate;

public interface Bostotte {
    String HUSBANKEN_TYPE = "husbanken";

    BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til);
}
