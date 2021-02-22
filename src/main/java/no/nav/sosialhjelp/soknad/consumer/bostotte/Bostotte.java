package no.nav.sosialhjelp.soknad.consumer.bostotte;

import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteDto;

import java.time.LocalDate;

public interface Bostotte {

    BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til);
}
