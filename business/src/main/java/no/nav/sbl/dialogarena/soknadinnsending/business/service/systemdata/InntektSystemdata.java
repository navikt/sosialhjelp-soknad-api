package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
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

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class InntektSystemdata implements Systemdata {

    @Inject
    UtbetalingService utbetalingService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalinger = innhentSystemregistrertInntekt(personIdentifikator);

        okonomiOpplysningUtbetalinger.removeIf(utbetaling -> utbetaling.getKilde().equals(JsonKilde.SYSTEM));
        if (systemUtbetalinger == null){
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setDriftsinformasjon("Kunne ikke hente utbetalinger fra NAV");
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalinger);
        }
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentSystemregistrertInntekt(String personIdentifikator){
        List<Utbetaling> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(personIdentifikator, LocalDate.now().minusDays(40), LocalDate.now());

        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(this::mapToJsonUtbetaling).collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonUtbetaling(Utbetaling utbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType("navytelse")
                .withTittel(utbetaling.type)
                .withBelop(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto)))
                .withNetto(utbetaling.netto)
                .withBrutto(utbetaling.brutto)
                .withSkattetrekk(utbetaling.skattetrekk)
                .withAndreTrekk(utbetaling.andreTrekk)
                .withUtbetalingsdato(utbetaling.utbetalingsdato.toString())
                .withPeriodeFom(utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                .withPeriodeTom(utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
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
