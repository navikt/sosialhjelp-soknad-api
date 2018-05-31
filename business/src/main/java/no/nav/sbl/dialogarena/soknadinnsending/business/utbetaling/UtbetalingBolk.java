package no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon.UTBETALING_BOLK;

@Service
public class UtbetalingBolk implements BolkService {

    @Inject
    UtbetalingService utbetalingService;

    @Override
    public String tilbyrBolk() {
        return UTBETALING_BOLK;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        List<Utbetaling> utbetalinger = utbetalingService.hentUtbetalingerForBrukerIPeriode(fodselsnummer, LocalDate.now().minusDays(30), LocalDate.now());

        return utbetalinger.stream()
                .map(utbetaling -> {
                    Faktum faktum = new Faktum()
                            .medSoknadId(soknadId)
                            .medType(SYSTEMREGISTRERT)
                            .medKey("utbetalinger.utbetaling")
                            .medUnikProperty("id")
                            .medSystemProperty("id", lagId(utbetaling))
                            .medSystemProperty("type", utbetaling.type)
                            .medSystemProperty("netto", utbetaling.netto + "")
                            .medSystemProperty("brutto", utbetaling.brutto + "")
                            .medSystemProperty("skatteTrekk", utbetaling.skatteTrekk + "")
                            .medSystemProperty("andreTrekk", utbetaling.andreTrekk + "")
                            .medSystemProperty("periodeFom", utbetaling.periodeFom != null ? utbetaling.periodeFom.toString() : null)
                            .medSystemProperty("periodeTom", utbetaling.periodeTom != null ? utbetaling.periodeTom.toString() : null)
                            .medSystemProperty("utbetalingsDato", utbetaling.utbetalingsDato.toString())
                            .medSystemProperty("komponenter", utbetaling.komponenter.size() + "");

                    for (int i = 0; i < utbetaling.komponenter.size(); i++) {
                        Utbetaling.Komponent komponent = utbetaling.komponenter.get(i);
                        String komponentNavn = "komponent_" + i + "_";
                        faktum
                                .medSystemProperty(komponentNavn + "type", komponent.type)
                                .medSystemProperty(komponentNavn + "belop", komponent.belop + "")
                                .medSystemProperty(komponentNavn + "satsType", komponent.satsType)
                                .medSystemProperty(komponentNavn + "satsBelop", komponent.satsBelop + "")
                                .medSystemProperty(komponentNavn + "satsAntall", komponent.satsAntall + "");

                    }
                    return faktum;
                }).collect(Collectors.toList());
    }

    private String lagId(Utbetaling utbetaling) {
        String id = utbetaling.type + "|" + utbetaling.bilagsNummer;

        if (utbetaling.utbetalingsDato != null) {
            id += "|" + utbetaling.utbetalingsDato.toString();
        }

        return id;
    }
}
