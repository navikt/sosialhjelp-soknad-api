package no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling;

import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSForespurtPeriode;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdent;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdentroller;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdenttyper;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UtbetalingService {

    private static final Logger logger = getLogger(UtbetalingService.class);

    @Inject
    private UtbetalingV1 utbetalingV1;

    public String hentUtbetalingerForBrukerIPeriode(String brukerFnr, LocalDate fom, LocalDate tom) {
        logger.info("Henter utbetalinger for {} i perioden {} til {}", brukerFnr, fom, tom);
        try {
            utbetalingV1.hentUtbetalingsinformasjon(
                    new WSHentUtbetalingsinformasjonRequest()
                            .withId(
                                    new WSIdent()
                                            .withIdentType(new WSIdenttyper().withValue("Personnr"))
                                            .withIdent(brukerFnr)
                                            .withRolle(new WSIdentroller().withValue("Rettighetshaver")))
                            .withPeriode(
                                    new WSForespurtPeriode()
                                            .withFom(tilDateTime(fom))
                                            .withTom(tilDateTime(tom))
                            )
            );
            return "ok";
        } catch (HentUtbetalingsinformasjonPeriodeIkkeGyldig | HentUtbetalingsinformasjonPersonIkkeFunnet | HentUtbetalingsinformasjonIkkeTilgang e) {
            logger.error("Kunne ikke hente utbetalinger for {}", brukerFnr, e);
        }

        return "feil";
    }

    private DateTime tilDateTime(LocalDate date) {
        return new DateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0);
    }
}
