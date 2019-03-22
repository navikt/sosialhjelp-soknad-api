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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.tilIntegerMedAvrunding;

@Component
public class InntektSystemdata implements Systemdata {

    @Inject
    UtbetalingService utbetalingService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        final String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        final List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalings = jsonData.getOkonomi().getOpplysninger()
                .getUtbetaling().stream().filter(utbetaling -> !utbetaling.getType().equals("navytelse"))
                .collect(Collectors.toList());
        final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = innhentSystemregistrertInntekt(personIdentifikator);
        if (utbetalinger == null){
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setDriftsinformasjon("Kunne ikke hente utbetalinger fra NAV");
        } else {
            okonomiOpplysningUtbetalings.removeAll(utbetalinger);
            okonomiOpplysningUtbetalings.addAll(utbetalinger);
        }
    }

    public List<JsonOkonomiOpplysningUtbetaling> innhentSystemregistrertInntekt(String personIdentifikator){
        Optional<List<Utbetaling>> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(personIdentifikator, LocalDate.now().minusDays(40), LocalDate.now());
        return utbetalinger.map(utbetalings -> utbetalings.stream().map(this::mapToJsonUtbetaling).collect(Collectors.toList())).orElse(null);
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonUtbetaling(Utbetaling utbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(utbetaling.type)
                .withTittel(utbetaling.tittel)
                .withBelop(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto)))
                .withNetto(utbetaling.netto)
                .withBrutto(utbetaling.brutto)
                .withSkattetrekk(utbetaling.skattetrekk)
                .withAndreTrekk(utbetaling.andreTrekk)
                .withUtbetalingsdato(utbetaling.utbetalingsdato.toString())
                .withPeriodeFom(utbetaling.periodeFom.toString())
                .withPeriodeTom(utbetaling.periodeTom.toString())
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
        return Collections.emptyList();
    }
}
