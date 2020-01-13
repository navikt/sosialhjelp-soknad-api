package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;

import java.time.LocalDate;

public interface Bostotte {

    BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til);
}
