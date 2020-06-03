package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt.SkattbarInntektService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SkattetatenSystemdata implements Systemdata {
    public static final Logger log = getLogger(SkattetatenSystemdata.class);

    @Inject
    SkattbarInntektService skattbarInntektService;

    @Inject
    OrganisasjonService organisasjonService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();

        fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger);

        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(false);
        if(soknadUnderArbeid.getHarSkattemeldingSamtykke()) {
            List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator);
            if (systemUtbetalingerSkattbar == null) {
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
            } else {
                okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar);
            }
        }
    }

    private void fjernGamleUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger) {
        okonomiOpplysningUtbetalinger.removeIf(
                utbetaling -> utbetaling.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN));
    }

    private List<JsonOkonomiOpplysningUtbetaling> innhentSkattbarSystemregistrertInntekt(String personIdentifikator) {
        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger(personIdentifikator);

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(this::mapToJsonOkonomiOpplysningUtbetaling).collect(Collectors.toList());
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

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(Utbetaling utbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN)
                .withTittel(utbetaling.tittel)
                .withBelop(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto)))
                .withNetto(utbetaling.netto)
                .withBrutto(utbetaling.brutto)
                .withSkattetrekk(utbetaling.skattetrekk)
                .withOrganisasjon(mapToJsonOrganisasjon(utbetaling.orgnummer))
                .withAndreTrekk(utbetaling.andreTrekk)
                .withPeriodeFom(utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                .withPeriodeTom(utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
                .withUtbetalingsdato(utbetaling.utbetalingsdato == null ? null : utbetaling.utbetalingsdato.toString())
                .withKomponenter(tilUtbetalingskomponentListe(utbetaling.komponenter))
                .withOverstyrtAvBruker(false);
    }

    private List<JsonOkonomiOpplysningUtbetalingKomponent> tilUtbetalingskomponentListe(List<Utbetaling.Komponent> komponenter) {
        if (komponenter != null) {
            return komponenter.stream().map(komponent ->
                    new JsonOkonomiOpplysningUtbetalingKomponent()
                            .withBelop(komponent.belop)
                            .withType(komponent.type)
                            .withSatsBelop(komponent.satsBelop)
                            .withSatsType(komponent.satsType)
                            .withSatsAntall(komponent.satsAntall)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static Integer tilIntegerMedAvrunding(String s) {
        Double d = tilDouble(s);
        if (d == null) {
            return null;
        }
        return (int) round(d);
    }

    private static Double tilDouble(String s) {
        if (isBlank(s)) {
            return null;
        }
        s = s.replaceAll(",", ".");
        s = s.replaceAll("\u00A0", "");
        return parseDouble(deleteWhitespace(s));
    }
}
