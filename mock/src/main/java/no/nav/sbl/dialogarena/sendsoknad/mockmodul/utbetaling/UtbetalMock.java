package no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UtbetalMock {

    private static final LocalDateTime POSTERINGSDATO = LocalDateTime.now().minusDays(40);
    private static final LocalDateTime UTBETALINGSDATO_INNENFOR_PERIODE = LocalDateTime.now().minusDays(30);
    private static final LocalDateTime UTBETALINGSDATO_INNENFOR_PERIODE2 = LocalDateTime.now().minusDays(15);
    private static final LocalDateTime UTBETALINGSDATO_UTENFOR_PERIODE = LocalDateTime.now().minusDays(35);
    private static final LocalDateTime FORFALLSDATO = LocalDateTime.now().minusDays(25);

    private static final Logger logger = LoggerFactory.getLogger(UtbetalMock.class);

    private static Map<String, WSHentUtbetalingsinformasjonResponse> responses = new HashMap<>();

    public UtbetalingV1 utbetalMock() {

        UtbetalingV1 mock = mock(UtbetalingV1.class);

        try {
            when(mock.hentUtbetalingsinformasjon(any(WSHentUtbetalingsinformasjonRequest.class)))
                    .thenAnswer((invocationOnMock -> getOrCreateCurrentUserResponse()));
        } catch (HentUtbetalingsinformasjonPeriodeIkkeGyldig | HentUtbetalingsinformasjonPersonIkkeFunnet
                | HentUtbetalingsinformasjonIkkeTilgang hentUtbetalingsinformasjonPeriodeIkkeGyldig) {
            hentUtbetalingsinformasjonPeriodeIkkeGyldig.printStackTrace();
        }

        return mock;
    }

    public static WSHentUtbetalingsinformasjonResponse getOrCreateCurrentUserResponse(){
        return responses.computeIfAbsent(OidcFeatureToggleUtils.getUserId(), k -> getDefaultResponse());
    }

    public static WSHentUtbetalingsinformasjonResponse getDefaultResponse(){
        return new WSHentUtbetalingsinformasjonResponse();
    }

    public static void setUtbetalinger(String jsonWSUtbetaling){

        WSHentUtbetalingsinformasjonResponse newResponse = new WSHentUtbetalingsinformasjonResponse();

        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(WSAktoer.class, new WSAktoerDeserializer());
            module.addDeserializer(DateTime.class, new CustomDateDeserializer());
            mapper.registerModule(module);

            WSUtbetaling wsUtbetaling = mapper.readValue(jsonWSUtbetaling, WSUtbetaling.class);

            newResponse.withUtbetalingListe(wsUtbetaling);

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Setter utbetalingsresponse: " + jsonWSUtbetaling);
        responses.replace(OidcFeatureToggleUtils.getUserId(), newResponse);
    }

    public static void resetUtbetalinger(){
        responses.replace(OidcFeatureToggleUtils.getUserId(), new WSHentUtbetalingsinformasjonResponse());
    }

    // Ikke i bruk, men har beholdt funksjonen her i tilfelle den kan bli nyttig senere.
    public static WSHentUtbetalingsinformasjonResponse getDefaultResponseWithUtbetalinger(){

        WSPerson person = new WSPerson()
                .withAktoerId("12345678910")
                .withNavn("Dummy");
        WSOrganisasjon dummyOrg = new WSOrganisasjon()
                .withAktoerId("000000000");
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("32902095534");

        WSHentUtbetalingsinformasjonResponse response = new WSHentUtbetalingsinformasjonResponse()
                .withUtbetalingListe(
                        new WSUtbetaling()
                                .withPosteringsdato(dato(POSTERINGSDATO))
                                .withUtbetaltTil(person)
                                .withUtbetalingNettobeloep(3880.0)
                                .withYtelseListe(
                                        new WSYtelse()
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
                                                .withRefundertForOrg(dummyOrg),
                                        new WSYtelse()
                                                .withYtelsestype(new WSYtelsestyper().withValue("Onkel Skrue penger"))
                                                .withYtelsesperiode(new WSPeriode()
                                                        .withFom(dato(2018, 2, 1))
                                                        .withTom(dato(2018, 2, 28)))
                                                .withYtelseskomponentListe(new WSYtelseskomponent()
                                                                .withYtelseskomponenttype("Sjekk")
                                                                .withSatsbeloep(0.0)
                                                                .withYtelseskomponentbeloep(10000.37),
                                                        new WSYtelseskomponent()
                                                                .withYtelseskomponenttype("Pengesekk")
                                                                .withSatstype("Dag")
                                                                .withSatsbeloep(5000.0)
                                                                .withSatsantall(10.0)
                                                                .withYtelseskomponentbeloep(50000.0))
                                                .withYtelseskomponentersum(3880.0)
                                                .withTrekksum(-500.0)
                                                .withSkattsum(-1337.0)
                                                .withYtelseNettobeloep(60000.0)
                                                .withBilagsnummer("568269566")
                                                .withRettighetshaver(person)
                                                .withRefundertForOrg(dummyOrg)
                                )
                                .withUtbetalingsdato(dato(UTBETALINGSDATO_INNENFOR_PERIODE))
                                .withForfallsdato(dato(FORFALLSDATO))
                                .withUtbetaltTilKonto(bankkonto)
                                .withUtbetalingsmelding(null)
                                .withUtbetalingsmetode("Norsk bankkonto")
                                .withUtbetalingsstatus("Utbetalt"),
                        new WSUtbetaling()
                                .withPosteringsdato(dato(POSTERINGSDATO))
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
                                .withUtbetalingsdato(dato(UTBETALINGSDATO_INNENFOR_PERIODE2))
                                .withForfallsdato(dato(FORFALLSDATO))
                                .withUtbetaltTilKonto(bankkonto)
                                .withUtbetalingsmetode("Norsk bankkonto")
                                .withUtbetalingsstatus("Utbetalt"),
                        new WSUtbetaling()
                                .withPosteringsdato(dato(POSTERINGSDATO))
                                .withUtbetaltTil(person)
                                .withUtbetalingNettobeloep(2000.0)
                                .withUtbetalingsmelding("Skal ikke vises fordi den er for gammel")
                                .withYtelseListe(new WSYtelse()
                                        .withYtelsestype(new WSYtelsestyper().withValue("Utdatert"))
                                        .withYtelsesperiode(new WSPeriode()
                                                .withFom(dato(2018, 2, 1))
                                                .withTom(dato(2018, 2, 28)))
                                        .withYtelseskomponentListe(new WSYtelseskomponent()
                                                .withYtelseskomponenttype("Arbeidstaker")
                                                .withSatsbeloep(1181.0)
                                                .withSatstype("Dag")
                                                .withYtelseskomponentbeloep(1000.0))
                                        .withYtelseskomponentersum(2000.0)
                                        .withTrekksum(-0.0)
                                        .withSkattListe(new WSSkatt().withSkattebeloep(-5419.0))
                                        .withSkattsum(-5419.0)
                                        .withYtelseNettobeloep(18201.0)
                                        .withBilagsnummer("568827358")
                                        .withRettighetshaver(person)
                                        .withRefundertForOrg(dummyOrg))
                                .withUtbetalingsdato(dato(UTBETALINGSDATO_UTENFOR_PERIODE))
                                .withForfallsdato(dato(FORFALLSDATO))
                                .withUtbetaltTilKonto(bankkonto)
                                .withUtbetalingsmetode("Norsk bankkonto")
                                .withUtbetalingsstatus("Utbetalt")
                );

        return response;
    }

    public static DateTime dato(LocalDateTime localDateTime) {
        return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static DateTime dato(int y, int m, int d) {
        return new DateTime(y, m, d, 0, 0, DateTimeZone.UTC);
    }
}
