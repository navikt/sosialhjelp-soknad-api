package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.skattbarinntekt.SkattbarInntektService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService.nowWithForcedNanoseconds;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SkattetatenSystemdata {
    public static final Logger log = getLogger(SkattetatenSystemdata.class);

    private final SkattbarInntektService skattbarInntektService;
    private final OrganisasjonService organisasjonService;
    private final TextService textService;

    public SkattetatenSystemdata(
            SkattbarInntektService skattbarInntektService,
            OrganisasjonService organisasjonService,
            TextService textService
    ) {
        this.skattbarInntektService = skattbarInntektService;
        this.organisasjonService = organisasjonService;
        this.textService = textService;
    }

    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();

        if(jsonData.getOkonomi().getOpplysninger().getBekreftelse().stream().anyMatch(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE) && bekreftelse.getVerdi())) {
            List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator);
            if (systemUtbetalingerSkattbar == null) {
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
            } else {
                jsonData.getOkonomi().getOpplysninger().getBekreftelse().stream()
                        .filter(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE))
                        .findAny()
                        .ifPresent(bekreftelse -> bekreftelse.withBekreftelsesDato(nowWithForcedNanoseconds()));
                fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger);
                okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar);
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(false);
            }
        } else { // Ikke samtykke!!!
            fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger);
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(false);
        }
        // Dette kan påvirke hvilke forventinger vi har til arbeidsforhold:
        ArbeidsforholdSystemdata.updateVedleggForventninger(soknadUnderArbeid.getJsonInternalSoknad(), textService);
    }

    private void fjernGamleUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger) {
        okonomiOpplysningUtbetalinger.removeIf(
                utbetaling -> utbetaling.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN));
    }

    private List<JsonOkonomiOpplysningUtbetaling> innhentSkattbarSystemregistrertInntekt(String personIdentifikator) {
        var utbetalinger = skattbarInntektService.hentUtbetalinger(personIdentifikator);
        if (utbetalinger == null) {
            return null;
        }

        return utbetalinger.stream()
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
    }

    private JsonOrganisasjon mapToJsonOrganisasjon(String orgnummer) {
        if (orgnummer == null) return null;

        if (orgnummer.matches("\\d{9}")) {
            return new JsonOrganisasjon()
                    .withNavn(organisasjonService.hentOrgNavn(orgnummer))
                    .withOrganisasjonsnummer(orgnummer);
        }

        if (orgnummer.matches("\\d{11}")) {
            log.info("Utbetalingens opplysningspliktigId er et personnummer. Dette blir ikke inkludert i soknad.json");
        } else {
            log.error("Utbetalingens opplysningspliktigId er verken et organisasjonsnummer eller personnummer: {}. Kontakt skatteetaten.", orgnummer);
        }

        return null;
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(no.nav.sosialhjelp.soknad.skattbarinntekt.domain.Utbetaling utbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN)
                .withTittel(utbetaling.getTittel())
//                .withBelop(null)
//                .withNetto(null)
                .withBrutto(utbetaling.getBrutto())
                .withSkattetrekk(utbetaling.getSkattetrekk())
                .withOrganisasjon(mapToJsonOrganisasjon(utbetaling.getOrgnummer()))
//                .withAndreTrekk(null)
                .withPeriodeFom(utbetaling.getPeriodeFom() != null ? utbetaling.getPeriodeFom().toString() : null)
                .withPeriodeTom(utbetaling.getPeriodeTom() != null ? utbetaling.getPeriodeTom().toString() : null)
//                .withUtbetalingsdato(null)
//                .withKomponenter(null)
                .withOverstyrtAvBruker(false);
    }
}
