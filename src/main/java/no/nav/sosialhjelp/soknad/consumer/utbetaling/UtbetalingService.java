package no.nav.sosialhjelp.soknad.consumer.utbetaling;

import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSAktoer;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSForespurtPeriode;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdent;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdentroller;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSIdenttyper;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSUtbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSYtelse;
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
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class UtbetalingService {

    private static final Logger logger = getLogger(UtbetalingService.class);

    @Inject
    private UtbetalingV1 utbetalingV1;

    @Cacheable("utbetalingCache")
    public List<Utbetaling> hentUtbetalingerForBrukerIPeriode(String brukerFnr, LocalDate fom, LocalDate tom) {
        logger.debug("Henter utbetalinger i perioden {} til {}", fom, tom);
        try {
            WSHentUtbetalingsinformasjonResponse wsUtbetalinger = utbetalingV1.hentUtbetalingsinformasjon(lagHentUtbetalingRequest(brukerFnr, fom, tom));
            List<Utbetaling> utbetalinger = mapTilUtbetalinger(wsUtbetalinger);
            logger.info("Antall navytelser utbetaling {}", utbetalinger.size());
            return utbetalinger;
        } catch (Exception e) {
            logger.warn("Kunne ikke hente utbetalinger", e);
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
                                .filter(utbetaltTilBruker(wsUtbetaling))
                                .map(ytelse -> ytelseTilUtbetaling(wsUtbetaling, ytelse)))
                .collect(toList());
    }

    private Predicate<WSYtelse> utbetaltTilBruker(WSUtbetaling wsUtbetaling) {
        return ytelse -> {
            String utbetaltTil = wsUtbetaling.getUtbetaltTil().getNavn();
            WSAktoer rettighetshaver = ytelse.getRettighetshaver();
            if (isNull(utbetaltTil) || isNull(rettighetshaver)) return false;

            String navn = rettighetshaver.getNavn();
            if (isNull(navn)) return false;

            return utbetaltTil.trim().equalsIgnoreCase(navn.trim());
        };
    }

    boolean utbetaltSisteFortiDager(WSUtbetaling wsUtbetaling) {
        return !tilLocalDate(wsUtbetaling.getUtbetalingsdato()).isBefore(LocalDate.now().minusDays(40));
    }

    Utbetaling ytelseTilUtbetaling(WSUtbetaling wsUtbetaling, WSYtelse ytelse) {
        Utbetaling utbetaling = new Utbetaling();

        utbetaling.type = "navytelse";
        utbetaling.tittel = ytelse.getYtelsestype() != null ? ytelse.getYtelsestype().getValue() : "";
        utbetaling.orgnummer = "889640782";

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
            logger.info("Antall navytelser komponent {}", ytelse.getYtelseskomponentListe().size());
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
