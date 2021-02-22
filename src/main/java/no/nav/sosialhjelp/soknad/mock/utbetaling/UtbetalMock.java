package no.nav.sosialhjelp.soknad.mock.utbetaling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSAktoer;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSBankkonto;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSUtbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSYtelse;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSYtelsestyper;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UtbetalMock {

    private static final Logger logger = LoggerFactory.getLogger(UtbetalMock.class);

    private static Map<String, WSHentUtbetalingsinformasjonResponse> responses = new HashMap<>();
    private static Map<String, Boolean> feilListe = new HashMap<>();
    private static String mockAltUtbetalingEndpoint;
    private static RestTemplate restTemplate;

    public UtbetalingV1 utbetalMock(String mockAltUtbetalingEndpoint, RestTemplate restTemplate) {
        initStatic(mockAltUtbetalingEndpoint, restTemplate);

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

    public static void initStatic(String newMockAltUtbetalingEndpoint, RestTemplate newRestTemplate) {
        mockAltUtbetalingEndpoint = newMockAltUtbetalingEndpoint;
        restTemplate = newRestTemplate;
    }

    public static WSHentUtbetalingsinformasjonResponse getOrCreateCurrentUserResponse() {
        if (MockUtils.isMockAltProfil()) {
            return getResponseFromMockAlt();
        }
        Boolean skalFeile = feilListe.get(SubjectHandler.getUserId());
        if (skalFeile != null && skalFeile) {
            throw new RuntimeException("Mock kall til NAV er satt til å feile!");
        }
        return responses.computeIfAbsent(SubjectHandler.getUserId(), k -> getDefaultResponse());
    }

    private static WSHentUtbetalingsinformasjonResponse getResponseFromMockAlt() {
        try {
            UriBuilder uri = UriBuilder.fromPath(mockAltUtbetalingEndpoint).queryParam("fnr", SubjectHandler.getUserId());
            RequestEntity<Void> request = RequestEntity.get(uri.build()).build();
            UtbetalingsListeDto utbetalingsListe = restTemplate.exchange(request, UtbetalingsListeDto.class).getBody();
            return mapToWsResponse(utbetalingsListe );
        } catch (ResourceAccessException e) {
            logger.error("Problemer med å hente mock utbetalinger!", e);
        } catch (HttpClientErrorException e) {
            logger.error("Problemer med å koble opp mot mock-alt!", e);
        } catch (HttpServerErrorException e) {
            logger.error("Problemer med å hente mock utbetalinger! Ekstern error: " + e.getMessage(), e);
        } catch (HttpMessageNotReadableException e) {
            logger.error("Problemer med å tolke data fra mock-alt!", e);
        }
        return null;
    }

    private static WSHentUtbetalingsinformasjonResponse mapToWsResponse(UtbetalingsListeDto utbetalingsListe) {
        WSHentUtbetalingsinformasjonResponse response = new WSHentUtbetalingsinformasjonResponse();
        response.withUtbetalingListe(
                utbetalingsListe.getUtbetalinger().stream()
                        .map(UtbetalMock::mapToWsResponsePart)
                        .collect(Collectors.toList())
        );
        return response;
    }

    private static WSUtbetaling mapToWsResponsePart(UtbetalingDto utbetaling) {
        WSAktoer mottaker = new MockWSAktoerImpl("Dummy Navn", SubjectHandler.getUserId());

        WSUtbetaling wsUtbetaling = new WSUtbetaling();
        wsUtbetaling.setForfallsdato(utbetaling.getDato());
        wsUtbetaling.setPosteringsdato(utbetaling.getDato());
        wsUtbetaling.setUtbetalingNettobeloep(utbetaling.getBelop());
        wsUtbetaling.setUtbetalingsdato(utbetaling.getDato());
        wsUtbetaling.setUtbetaltTil(mottaker);
        wsUtbetaling.setUtbetaltTilKonto(new WSBankkonto());

        WSYtelse ytelse = new WSYtelse();
        ytelse.setRettighetshaver(mottaker);
        ytelse.setYtelseNettobeloep(utbetaling.getBelop());
        ytelse.setYtelseskomponentersum(utbetaling.getBelop());
        WSYtelsestyper ytelsestype = new WSYtelsestyper();
        ytelsestype.setValue(utbetaling.getYtelsestype());
        ytelse.setYtelsestype(ytelsestype);
        wsUtbetaling.getYtelseListe().add(ytelse);
        return wsUtbetaling;
    }

    public static WSHentUtbetalingsinformasjonResponse getDefaultResponse() {
        return new WSHentUtbetalingsinformasjonResponse();
    }

    public static void setUtbetalinger(String jsonWSUtbetaling) {

        WSHentUtbetalingsinformasjonResponse newResponse = new WSHentUtbetalingsinformasjonResponse();

        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(WSAktoer.class, new WSAktoerDeserializer());
            module.addDeserializer(DateTime.class, new CustomDateDeserializer());
            mapper.registerModule(module);

            TypeReference<Collection<WSUtbetaling>> typeRef
                    = new TypeReference<Collection<WSUtbetaling>>() {
            };

            Collection<WSUtbetaling> wsUtbetaling = mapper.readValue(jsonWSUtbetaling, typeRef);

            newResponse.withUtbetalingListe(wsUtbetaling);

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Setter utbetalingsresponse: " + jsonWSUtbetaling);
        String fnr = SubjectHandler.getUserId();
        responses.remove(fnr);
        responses.put(fnr, newResponse);
    }

    public static void setMockSkalFeile(String fnr, boolean skalFeile) {
        feilListe.remove(fnr);
        feilListe.put(fnr, skalFeile);
    }

    public static DateTime dato(LocalDateTime localDateTime) {
        return new DateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static DateTime dato(int y, int m, int d) {
        return new DateTime(y, m, d, 0, 0, DateTimeZone.UTC);
    }

    private static class UtbetalingsListeDto {
        private List<UtbetalingDto> utbetalinger = new ArrayList<>();

        public List<UtbetalingDto> getUtbetalinger() {
            return utbetalinger;
        }

        public void setUtbetalinger(List<UtbetalingDto> utbetalinger) {
            this.utbetalinger = utbetalinger;
        }
    }

    @SuppressWarnings("unused")
    private static class UtbetalingDto {
        private double belop;
        private DateTime dato;
        private String ytelsestype;

        public double getBelop() {
            return belop;
        }

        public DateTime getDato() {
            return dato;
        }

        public String getYtelsestype() {
            return ytelsestype;
        }
    }

    private static class MockWSAktoerImpl extends WSAktoer {
        public MockWSAktoerImpl(String mottakerNavn, String mottakerFnr) {
            this.setNavn(mottakerNavn);
            this.setAktoerId(mottakerFnr);
        }
    }
}
