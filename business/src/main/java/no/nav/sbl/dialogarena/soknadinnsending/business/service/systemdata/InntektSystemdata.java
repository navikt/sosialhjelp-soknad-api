package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class InntektSystemdata implements Systemdata {
    public static final Logger log = getLogger(InntektSystemdata.class);

    @Inject
    UtbetalingService utbetalingService;

    @Inject
    SkattbarInntektService skattbarInntektService;

    @Inject
    OrganisasjonService organisasjonService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerNav = innhentNavSystemregistrertInntekt(personIdentifikator);
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator);

        fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setUtbetalingerFraNavFeilet(false);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(false);
        if (systemUtbetalingerNav == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setUtbetalingerFraNavFeilet(true);
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerNav);
        }

        if (systemUtbetalingerSkattbar == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar);
        }
    }

    private void fjernGamleUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger) {
        okonomiOpplysningUtbetalinger.removeIf(
                utbetaling -> utbetaling.getType().equalsIgnoreCase(UTBETALING_NAVYTELSE) ||
                        utbetaling.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN));
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentNavSystemregistrertInntekt(String personIdentifikator) {
        List<Utbetaling> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(personIdentifikator, LocalDate.now().minusDays(40), LocalDate.now());

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(utbetaling -> mapToJsonOkonomiOpplysningUtbetaling(utbetaling, UTBETALING_NAVYTELSE)).collect(Collectors.toList());
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentSkattbarSystemregistrertInntekt(String personIdentifikator) {
        List<Utbetaling> utbetalinger = skattbarInntektService.hentSkattbarInntekt(personIdentifikator);

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(utbetaling -> mapToJsonOkonomiOpplysningUtbetaling(utbetaling, UTBETALING_SKATTEETATEN)).collect(Collectors.toList());
    }

    JsonOrganisasjon mapToJsonOrganisasjon(String orgnummer) {
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

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(Utbetaling utbetaling, String type) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(type)
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

    static Integer tilIntegerMedAvrunding(String s) {
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
