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

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon.UTBETALING_BOLK;

@Service
public class UtbetalingBolk implements BolkService {

    public static final NumberFormat UTBETALING_FORMATTER = new DecimalFormat("##,##0.00");

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

        if (utbetalinger == null) {
            fakta.add(new Faktum()
                    .medSoknadId(soknadId)
                    .medType(SYSTEMREGISTRERT)
                    .medKey("utbetalinger.feilet")
                    .medValue("true"));
        } else {
            fakta.add(lagHarUtbetalingerFaktum(!utbetalinger.isEmpty()));
            for (Utbetaling utbetaling : utbetalinger) {
                fakta.addAll(lagFaktumForUtbetaling(soknadId, utbetaling));
            }
        }

        return fakta;
    }

    private Faktum lagHarUtbetalingerFaktum(boolean harUtbetalinger) {
        return new Faktum()
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.ingen")
                .medValue(!harUtbetalinger + "");
    }

    private List<Faktum> lagFaktumForUtbetaling(Long soknadId, Utbetaling utbetaling) {
        List<Faktum> utbetalingsfakta = new ArrayList<>();
        String utbetalingsid = lagId(utbetaling);
        utbetalingsfakta.add(new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.utbetaling")
                .medUnikProperty("id")
                .medSystemProperty("id", utbetalingsid)
                .medSystemProperty("utbetalingsid", utbetalingsid)
                .medSystemProperty("type", utbetaling.type)
                .medSystemProperty("netto", formatTall(utbetaling.netto))
                .medSystemProperty("brutto", formatTall(utbetaling.brutto))
                .medSystemProperty("skatteTrekk", formatTall(utbetaling.skatteTrekk))
                .medSystemProperty("andreTrekk", formatTall(utbetaling.andreTrekk))
                .medSystemProperty("periodeFom", utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                .medSystemProperty("periodeTom", utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
                .medSystemProperty("utbetalingsDato", utbetaling.utbetalingsDato != null ? utbetaling.utbetalingsDato.toString() : null));

        if (utbetaling.komponenter != null) {
            for (int i = 0; i < utbetaling.komponenter.size(); i++) {
                Utbetaling.Komponent komponent = utbetaling.komponenter.get(i);
                utbetalingsfakta.add(new Faktum()
                        .medSoknadId(soknadId)
                        .medType(SYSTEMREGISTRERT)
                        .medKey("utbetalinger.utbetaling.komponent")
                        .medUnikProperty("id")
                        .medSystemProperty("id", i + "")
                        .medSystemProperty("utbetalingsid", utbetalingsid)
                        .medSystemProperty("type", komponent.type)
                        .medSystemProperty("belop", formatTall(komponent.belop))
                        .medSystemProperty("satstype", komponent.satsType)
                        .medSystemProperty("satsbelop", formatTall(komponent.satsBelop))
                        .medSystemProperty("satsantall", formatTall(komponent.satsAntall)));
            }
        }
        return utbetalingsfakta;
    }

    private String lagId(Utbetaling utbetaling) {
        String id = utbetaling.type + "|" + utbetaling.bilagsNummer;

        if (utbetaling.utbetalingsDato != null) {
            id += "|" + utbetaling.utbetalingsDato.toString();
        }

        return id;
    }

    private String formatTall(double d) {
        return UTBETALING_FORMATTER.format(d);
    }
}
