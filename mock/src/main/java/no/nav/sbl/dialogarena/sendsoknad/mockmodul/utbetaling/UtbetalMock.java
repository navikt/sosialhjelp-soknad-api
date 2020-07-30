package no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.common.auth.SubjectHandler;

import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonIkkeTilgang;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPeriodeIkkeGyldig;
import no.nav.tjeneste.virksomhet.utbetaling.v1.HentUtbetalingsinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSAktoer;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSUtbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonRequest;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UtbetalMock {

    private static final Logger logger = LoggerFactory.getLogger(UtbetalMock.class);

    private static Map<String, WSHentUtbetalingsinformasjonResponse> responses = new HashMap<>();
    private static Map<String, Boolean> feilListe = new HashMap<>();

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
        if(feilListe.get(SubjectHandler.getIdent().orElse(null))) {
            throw new RuntimeException("Mock kall til NAV er satt til å feile!");
        }
        return responses.computeIfAbsent(SubjectHandler.getIdent().orElse(null), k -> getDefaultResponse());
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

            TypeReference<Collection<WSUtbetaling>> typeRef
                    = new TypeReference<Collection<WSUtbetaling>>() {};

            Collection<WSUtbetaling> wsUtbetaling = mapper.readValue(jsonWSUtbetaling, typeRef);

            newResponse.withUtbetalingListe(wsUtbetaling);

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Setter utbetalingsresponse: " + jsonWSUtbetaling);
        String fnr = SubjectHandler.getIdent().orElse(null);
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
}
