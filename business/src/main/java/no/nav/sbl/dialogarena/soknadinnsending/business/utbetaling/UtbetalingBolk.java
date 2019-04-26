package no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
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
    public static final int NAVUTBETALINGER_DAGER = 40;
    public static final String SKATTEETATEN = "skatteetaten";

    @Inject
    UtbetalingService utbetalingService;

    @Inject
    ArbeidsforholdTransformer arbeidsforholdTransformer;

    @Inject
    SkattbarInntektService skattbarInntektService;

    @Override
    public String tilbyrBolk() {
        return UTBETALING_BOLK;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        List<Utbetaling> utbetalingerFraNav = utbetalingService.hentUtbetalingerForBrukerIPeriode(fodselsnummer, LocalDate.now().minusDays(NAVUTBETALINGER_DAGER), LocalDate.now());
        List<Utbetaling> utbetalingerRegistrertHosSkatteetaten = skattbarInntektService.hentSkattbarInntekt(fodselsnummer);

        List<Faktum> fakta = new ArrayList<>();

        if (utbetalingerFraNav != null && !utbetalingerFraNav.isEmpty()) {
            fakta.add(lagHarUtbetalingerFaktum(true));
        } else if (utbetalingerRegistrertHosSkatteetaten != null && !utbetalingerRegistrertHosSkatteetaten.isEmpty()) {
            fakta.add(lagHarUtbetalingerFaktum(true));
        } else {
            fakta.add(lagHarUtbetalingerFaktum(false));
        }

        if (utbetalingerFraNav == null) {
            fakta.add(new Faktum()
                    .medSoknadId(soknadId)
                    .medType(SYSTEMREGISTRERT)
                    .medKey("utbetalinger.fra.nav.feilet")
                    .medValue("true"));
        } else {
            utbetalingerFraNav.stream().map(utbetaling -> lagFaktumForUtbetaling(soknadId, utbetaling, "navytelse")).forEach(fakta::addAll);
        }

        if (utbetalingerRegistrertHosSkatteetaten == null) {
            fakta.add(new Faktum()
                    .medSoknadId(soknadId)
                    .medType(SYSTEMREGISTRERT)
                    .medKey("utbetalinger.fra.skatteetaten.feilet")
                    .medValue("true"));
        } else {
            utbetalingerRegistrertHosSkatteetaten.stream().map(utbetaling -> lagFaktumForUtbetaling(soknadId, utbetaling, SKATTEETATEN)).forEach(fakta::addAll);
        }

        return fakta;
    }

    private Faktum lagHarUtbetalingerFaktum(boolean harUtbetalinger) {
        return new Faktum()
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.ingen")
                .medValue(!harUtbetalinger + "");
    }

    private List<Faktum> lagFaktumForUtbetaling(Long soknadId, Utbetaling utbetaling, String kildeType) {
        List<Faktum> utbetalingsfakta = new ArrayList<>();
        String utbetalingsid = lagId(utbetaling);
        utbetalingsfakta.add(new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.utbetaling")
                .medUnikProperty("id")
                .medSystemProperty("id", utbetalingsid)
                .medSystemProperty("utbetalingsid", utbetalingsid)
                .medSystemProperty("type", utbetaling.tittel)
                .medSystemProperty("kildeType", kildeType)
                .medSystemProperty("organisasjon", arbeidsforholdTransformer.hentOrgNavn(utbetaling.orgnummer))
                .medSystemProperty("organisasjonsnummer", utbetaling.orgnummer)
                .medSystemProperty("netto", formatTall(utbetaling.netto))
                .medSystemProperty("brutto", formatTall(utbetaling.brutto))
                .medSystemProperty("skattetrekk", formatTall(utbetaling.skattetrekk))
                .medSystemProperty("andretrekk", formatTall(utbetaling.andreTrekk))
                .medSystemProperty("periodeFom", utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                .medSystemProperty("periodeTom", utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
                .medSystemProperty("utbetalingsDato", utbetaling.utbetalingsdato != null ? utbetaling.utbetalingsdato.toString() : null));

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
        String id = utbetaling.tittel + "|" + utbetaling.bilagsnummer + "|" + utbetaling.orgnummer;

        if (utbetaling.utbetalingsdato != null) {
            id += "|" + utbetaling.utbetalingsdato.toString();
        }

        return id;
    }

    private String formatTall(double d) {
        return UTBETALING_FORMATTER.format(d);
    }
}
