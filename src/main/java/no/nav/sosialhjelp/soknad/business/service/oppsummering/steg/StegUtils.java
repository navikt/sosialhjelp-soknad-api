package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StegUtils {

    private StegUtils() {
        // no-op
    }

    public static String fulltnavn(JsonNavn navn) {
        var optionalFornavn = Optional.ofNullable(navn.getFornavn());
        var optionalMellomnavn = Optional.ofNullable(navn.getMellomnavn());
        var optionalEtternavn = Optional.ofNullable(navn.getEtternavn());

        return Stream.of(optionalFornavn, optionalMellomnavn, optionalEtternavn)
                .map(opt -> opt.orElse(""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
