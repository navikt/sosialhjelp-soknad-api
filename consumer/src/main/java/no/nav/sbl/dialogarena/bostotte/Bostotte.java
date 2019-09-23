package no.nav.sbl.dialogarena.bostotte;

import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;

import java.time.LocalDate;

public interface Bostotte {
    String HUSBANKEN_TYPE = "husbanken";

    BostotteDto hentBostotte(String personIdentifikator, LocalDate fra, LocalDate til);
}
