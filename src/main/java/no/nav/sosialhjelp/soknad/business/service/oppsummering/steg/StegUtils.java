package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Svar;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

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

    public static boolean isNotNullOrEmtpy(String s) {
        return s != null && !s.isEmpty();
    }

    public static Sporsmal integerVerdiSporsmalMedTittel(String tittel, String key, Integer verdi) {
        return new Sporsmal.Builder()
                .withTittel(tittel)
                .withErUtfylt(verdi != null)
                .withFelt(verdi != null ?
                        singletonList(
                                new Felt.Builder()
                                        .withLabel(key)
                                        .withSvar(createSvar(verdi.toString(), SvarType.TEKST))
                                        .withType(Type.TEKST)
                                        .build()) :
                        null)
                .build();
    }

    public static List<Felt> booleanVerdiFelt(boolean harSvartJa, String keyTrue, String keyFalse) {
        return singletonList(
                new Felt.Builder()
                        .withType(Type.CHECKBOX)
                        .withSvar(createSvar(harSvartJa ? keyTrue : keyFalse, SvarType.LOCALE_TEKST))
                        .build()
        );
    }

    public static boolean harBarnMedKilde(JsonForsorgerplikt forsorgerplikt, JsonKilde kilde) {
        var harForsorgerplikt = forsorgerplikt != null && forsorgerplikt.getHarForsorgerplikt() != null && forsorgerplikt.getHarForsorgerplikt().getVerdi().equals(Boolean.TRUE);

        return harForsorgerplikt && forsorgerplikt.getHarForsorgerplikt().getKilde().equals(kilde) &&
                forsorgerplikt.getAnsvar() != null && forsorgerplikt.getAnsvar().stream().anyMatch(barn -> barn.getBarn().getKilde().equals(kilde));
    }

    public static Svar createSvar(String value, SvarType type) {
        return new Svar.Builder()
                .withValue(value)
                .withType(type)
                .build();
    }
}
