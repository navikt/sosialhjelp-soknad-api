package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.tilIntegerMedAvrunding;

@Component
public class InntektSystemdata implements Systemdata {

    @Inject
    UtbetalingService utbetalingService;

    @Inject
    SkattbarInntektService skattbarInntektService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerNav = innhentNavSystemregistrertInntekt(personIdentifikator);
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerSkattbar = innhentSkattbarSystemregistrertInntekt(personIdentifikator);

        okonomiOpplysningUtbetalinger.removeIf(utbetaling -> utbetaling.getKilde().equals(JsonKilde.SYSTEM));
        if (systemUtbetalingerNav == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setDriftsinformasjon("Kunne ikke hente utbetalinger fra NAV");
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerNav);
        }

        if (systemUtbetalingerSkattbar == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setDriftsinformasjon("Kunne ikke hente skattbar inntekt fra Skatteetaten");
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerSkattbar);
        }

        if (systemUtbetalingerNav == null && systemUtbetalingerSkattbar == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setDriftsinformasjon("Kunne ikke hente utbetalinger fra NAV og kunne ikke hente skattbar inntekt fra Skatteetaten");
        }
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentNavSystemregistrertInntekt(String personIdentifikator) {
        List<Utbetaling> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(personIdentifikator, LocalDate.now().minusDays(40), LocalDate.now());

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(utbetaling -> mapToJsonUtbetaling(utbetaling, "navytelse")).collect(Collectors.toList());
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentSkattbarSystemregistrertInntekt(String personIdentifikator) {
        List<Utbetaling> utbetalinger = skattbarInntektService.hentSkattbarInntekt(personIdentifikator);

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(utbetaling -> mapToJsonUtbetaling(utbetaling, "skatteetaten")).collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonUtbetaling(Utbetaling utbetaling, String type) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(type)
                .withTittel(utbetaling.type)
                .withBelop(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto)))
                .withNetto(utbetaling.netto)
                .withBrutto(utbetaling.brutto)
                .withSkattetrekk(utbetaling.skattetrekk)
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
}
