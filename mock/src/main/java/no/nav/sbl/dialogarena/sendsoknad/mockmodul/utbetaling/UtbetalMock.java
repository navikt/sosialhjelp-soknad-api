package no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling;

import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class UtbetalMock implements UtbetalingV1 {

    private static final Logger logger = getLogger(UtbetalMock.class);


    @Override
    public void ping() {
        logger.info("Pinger mock");
    }

    @Override
    public WSHentUtbetalingsinformasjonResponse hentUtbetalingsinformasjon(WSHentUtbetalingsinformasjonRequest req) throws HentUtbetalingsinformasjonPeriodeIkkeGyldig, HentUtbetalingsinformasjonPersonIkkeFunnet, HentUtbetalingsinformasjonIkkeTilgang {
        logger.info("Mocker svar på request: Id=(ident={}, type={}, rolle={}), Periode=(fom={}, tom={}), ytelsetyper={} ",
                req.getId().getIdent(), req.getId().getIdentType().getValue(), req.getId().getRolle().getValue(),
                req.getPeriode().getFom(), req.getPeriode().getTom(),
                Arrays.toString(req.getYtelsestypeListe().toArray())
        );

        WSPerson person = new WSPerson()
                .withAktoerId(req.getId().getIdent())
                .withNavn("Dummy");
        WSOrganisasjon dummyOrg = new WSOrganisasjon()
                .withAktoerId("000000000");
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("32902095534");

        return new WSHentUtbetalingsinformasjonResponse()
                .withUtbetalingListe(
                        new WSUtbetaling()
                                .withPosteringsdato(dato(2018, 2, 21))
                                .withUtbetaltTil(person)
                                .withUtbetalingNettobeloep(3880.0)
                                .withYtelseListe(new WSYtelse()
                                        .withYtelsestype(new WSYtelsestyper().withValue("Barnetrygd"))
                                        .withYtelsesperiode(new WSPeriode()
                                                .withFom(dato(2018, 2, 1))
                                                .withTom(dato(2018, 2, 28)))
                                        .withYtelseskomponentListe(new WSYtelseskomponent()
                                                .withYtelseskomponenttype("Ordinær og utvidet")
                                                .withSatsbeloep(0.0)
                                                .withYtelseskomponentbeloep(3880.0))
                                        .withYtelseskomponentersum(3880.0)
                                        .withTrekksum(-0.0)
                                        .withSkattsum(-0.0)
                                        .withYtelseNettobeloep(3880.0)
                                        .withBilagsnummer("568269505")
                                        .withRettighetshaver(person)
                                        .withRefundertForOrg(dummyOrg)
                                )
                                .withUtbetalingsdato(dato(2018, 2, 27))
                                .withForfallsdato(dato(2018, 2, 28))
                                .withUtbetaltTilKonto(bankkonto)
                                .withUtbetalingsmelding(null)
                                .withUtbetalingsmetode("Norsk bankkonto")
                                .withUtbetalingsstatus("Utbetalt"),
                        new WSUtbetaling()
                                .withPosteringsdato(dato(2018, 2, 21))
                                .withUtbetaltTil(person)
                                .withUtbetalingNettobeloep(18201.0)
                                .withUtbetalingsmelding("Skatt tabell: 7103, NAV får overført ditt skattekort fra Skatteetaten og er pliktig til, å bruke dette skattekortet ...")
                                .withYtelseListe(new WSYtelse()
                                        .withYtelsestype(new WSYtelsestyper().withValue("Sykepenger"))
                                        .withYtelsesperiode(new WSPeriode()
                                                .withFom(dato(2018, 2, 1))
                                                .withTom(dato(2018, 2, 28)))
                                        .withYtelseskomponentListe(new WSYtelseskomponent()
                                                .withYtelseskomponenttype("Arbeidstaker")
                                                .withSatsbeloep(1181.0)
                                                .withSatstype("Dag")
                                                .withYtelseskomponentbeloep(23620.0))
                                        .withYtelseskomponentersum(23620.0)
                                        .withTrekksum(-0.0)
                                        .withSkattListe(new WSSkatt().withSkattebeloep(-5419.0))
                                        .withSkattsum(-5419.0)
                                        .withYtelseNettobeloep(18201.0)
                                        .withBilagsnummer("568827408")
                                        .withRettighetshaver(person)
                                        .withRefundertForOrg(dummyOrg))
                                .withUtbetalingsdato(dato(2018, 2, 22))
                                .withForfallsdato(dato(2018, 2, 22))
                                .withUtbetaltTilKonto(bankkonto)
                                .withUtbetalingsmetode("Norsk bankkonto")
                                .withUtbetalingsstatus("Utbetalt")

                );
    }

    private DateTime dato(int y, int m, int d) {
        return new DateTime(y, m, d, 0, 0, DateTimeZone.UTC);
    }
}
