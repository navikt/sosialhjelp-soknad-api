package no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UtbetalingService {

    private static final Logger logger = getLogger(UtbetalingService.class);

    @Inject
    private UtbetalingV1 utbetalingV1;

    @Cacheable("utbetalingCache")
    public List<Utbetaling> hentUtbetalingerForBrukerIPeriode(String brukerFnr, LocalDate fom, LocalDate tom) {
        logger.info("Henter utbetalinger for {} i perioden {} til {}", brukerFnr, fom, tom);
        try {
            WSHentUtbetalingsinformasjonResponse wsUtbetalinger = utbetalingV1.hentUtbetalingsinformasjon(lagHentUtbetalingRequest(brukerFnr, fom, tom));
            return mapTilUtbetalinger(wsUtbetalinger);
        } catch (Exception e) {
            logger.warn("Kunne ikke hente utbetalinger for {}", brukerFnr, e);
            return null;
        }

    }

    private WSHentUtbetalingsinformasjonRequest lagHentUtbetalingRequest(String brukerFnr, LocalDate fom, LocalDate tom) {
        return new WSHentUtbetalingsinformasjonRequest()
                .withId(
                        new WSIdent()
                                .withIdentType(new WSIdenttyper().withValue("Personnr"))
                                .withIdent(brukerFnr)
                                .withRolle(new WSIdentroller().withValue("Rettighetshaver")))
                .withPeriode(
                        new WSForespurtPeriode()
                                .withFom(tilDateTime(fom))
                                .withTom(tilDateTime(tom))
                );
    }

    List<Utbetaling> mapTilUtbetalinger(WSHentUtbetalingsinformasjonResponse wsUtbetalinger) {
        if (wsUtbetalinger == null || wsUtbetalinger.getUtbetalingListe() == null) {
            return new ArrayList<>();
        }

        return wsUtbetalinger.getUtbetalingListe().stream()
                .filter(wsUtbetaling -> wsUtbetaling.getUtbetalingsdato() != null)
                .filter(this::utbetaltSisteFortiDager)
                .flatMap(wsUtbetaling ->
                        wsUtbetaling.getYtelseListe()
                                .stream()
                                .map(ytelse -> ytelseTilUtbetaling(wsUtbetaling, ytelse)))
                .collect(toList());
    }

    boolean utbetaltSisteFortiDager(WSUtbetaling wsUtbetaling) {
        return !tilLocalDate(wsUtbetaling.getUtbetalingsdato()).isBefore(LocalDate.now().minusDays(40));
    }

    Utbetaling ytelseTilUtbetaling(WSUtbetaling wsUtbetaling, WSYtelse ytelse) {
        Utbetaling utbetaling = new Utbetaling();

        utbetaling.type = ytelse.getYtelsestype() != null ? ytelse.getYtelsestype().getValue() : "";
        utbetaling.netto = ytelse.getYtelseNettobeloep();
        utbetaling.brutto = ytelse.getYtelseskomponentersum();
        utbetaling.skattetrekk = ytelse.getSkattsum();
        utbetaling.andreTrekk = ytelse.getTrekksum();
        utbetaling.bilagsnummer = ytelse.getBilagsnummer();

        WSPeriode wsPeriode = ytelse.getYtelsesperiode();
        if (wsPeriode != null) {
            utbetaling.periodeFom = tilLocalDate(wsPeriode.getFom());
            utbetaling.periodeTom = tilLocalDate(wsPeriode.getTom());
        }

        utbetaling.utbetalingsdato = tilLocalDate(wsUtbetaling.getUtbetalingsdato());

        if (ytelse.getYtelseskomponentListe() != null) {
            utbetaling.komponenter = ytelse.getYtelseskomponentListe().stream()
                    .map(wsYtelseskomponent -> {
                        Utbetaling.Komponent komponent = new Utbetaling.Komponent();

                        komponent.type = wsYtelseskomponent.getYtelseskomponenttype();
                        komponent.belop = nullSafe(wsYtelseskomponent.getYtelseskomponentbeloep());

                        komponent.satsType = wsYtelseskomponent.getSatstype();
                        komponent.satsBelop = nullSafe(wsYtelseskomponent.getSatsbeloep());
                        komponent.satsAntall = nullSafe(wsYtelseskomponent.getSatsantall());

                        return komponent;
                    }).collect(toList());
        }

        return utbetaling;
    }


    private DateTime tilDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return new DateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0);
    }

    private LocalDate tilLocalDate(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }

    private double nullSafe(Double d) {
        return d != null ? d : 0;
    }

}
