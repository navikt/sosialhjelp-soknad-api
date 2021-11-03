package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;

public final class InntektFormueUtils {

    private InntektFormueUtils() {
        // no-op
    }

    public static boolean harBekreftelse(JsonOkonomiopplysninger opplysninger, String type) {
        return opplysninger.getBekreftelse() != null && opplysninger.getBekreftelse().stream().anyMatch(bekreftelse -> type.equals(bekreftelse.getType()));
    }

    public static boolean harBekreftelseTrue(JsonOkonomiopplysninger opplysninger, String type) {
        return opplysninger.getBekreftelse().stream().anyMatch(bekreftelse -> type.equals(bekreftelse.getType()) && Boolean.TRUE.equals(bekreftelse.getVerdi()));
    }

    public static JsonOkonomibekreftelse getBekreftelse(JsonOkonomiopplysninger opplysninger, String type) {
        return opplysninger.getBekreftelse().stream().filter(bekreftelse -> type.equals(bekreftelse.getType())).findFirst().orElse(null);
    }

    public static boolean harValgtFormueType(JsonOkonomioversikt oversikt, String type) {
        return oversikt.getFormue().stream()
                .anyMatch(formue -> type.equals(formue.getType()));
    }

    public static void addFormueIfPresent(JsonOkonomioversikt oversikt, List<Felt> felter, String type, String key) {
        oversikt.getFormue().stream()
                .filter(formue -> type.equals(formue.getType()))
                .findFirst()
                .ifPresent(formue -> felter.add(
                        new Felt.Builder()
                                .withType(Type.CHECKBOX)
                                .withSvar(createSvar(key, SvarType.LOCALE_TEKST))
                                .build()
                ));
    }
}
