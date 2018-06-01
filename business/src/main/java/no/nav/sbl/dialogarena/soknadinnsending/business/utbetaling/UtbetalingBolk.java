package no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon.UTBETALING_BOLK;

@Service
public class UtbetalingBolk implements BolkService {

    private static final NumberFormat df = new DecimalFormat("#.00");

    @Inject
    UtbetalingService utbetalingService;

    @Override
    public String tilbyrBolk() {
        return UTBETALING_BOLK;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        List<Utbetaling> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(fodselsnummer, LocalDate.now().minusDays(30), LocalDate.now());


        List<Faktum> fakta = new ArrayList<>();

        fakta.add(lagHarUtbetalingerFaktum(!utbetalinger.isEmpty()));

        fakta.addAll(utbetalinger.stream()
                .map(utbetaling -> lagFaktumForUtbetaling(soknadId, utbetaling))
                .collect(Collectors.toList()));

        return fakta;
    }

    private Faktum lagHarUtbetalingerFaktum(boolean harUtbetalinger) {
        return new Faktum()
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.ingen")
                .medValue(!harUtbetalinger + "");
    }

    private Faktum lagFaktumForUtbetaling(Long soknadId, Utbetaling utbetaling) {
        Faktum faktum = new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.utbetaling")
                .medUnikProperty("id")
                .medSystemProperty("id", lagId(utbetaling))
                .medSystemProperty("type", utbetaling.type)
                .medSystemProperty("netto", formatTall(utbetaling.netto))
                .medSystemProperty("brutto", formatTall(utbetaling.brutto))
                .medSystemProperty("skatteTrekk", formatTall(utbetaling.skatteTrekk))
                .medSystemProperty("andreTrekk", formatTall(utbetaling.andreTrekk))
                .medSystemProperty("periodeFom", utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                .medSystemProperty("periodeTom", utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
                .medSystemProperty("utbetalingsDato", utbetaling.utbetalingsDato.toString())
                .medSystemProperty("komponenter", utbetaling.komponenter.size() + "");

        for (int i = 0; i < utbetaling.komponenter.size(); i++) {
            Utbetaling.Komponent komponent = utbetaling.komponenter.get(i);
            String komponentNavn = "komponent_" + i + "_";
            faktum
                    .medSystemProperty(komponentNavn + "type", komponent.type)
                    .medSystemProperty(komponentNavn + "belop", formatTall(komponent.belop))
                    .medSystemProperty(komponentNavn + "satsType", komponent.satsType)
                    .medSystemProperty(komponentNavn + "satsBelop", formatTall(komponent.satsBelop))
                    .medSystemProperty(komponentNavn + "satsAntall", formatTall(komponent.satsAntall));

        }
        return faktum;
    }

    private String lagId(Utbetaling utbetaling) {
        String id = utbetaling.type + "|" + utbetaling.bilagsNummer;

        if (utbetaling.utbetalingsDato != null) {
            id += "|" + utbetaling.utbetalingsDato.toString();
        }

        return id;
    }

    private String formatTall(double d) {
        return df.format(d);
    }
}
