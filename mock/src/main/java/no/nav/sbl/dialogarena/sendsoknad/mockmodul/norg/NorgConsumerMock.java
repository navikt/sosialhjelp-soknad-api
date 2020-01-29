package no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NorgConsumerMock {

    private static Map<String, Map<String, RsNorgEnhet>> responses = new HashMap<>();

    public NorgConsumer norgConsumerMock(){
        NorgConsumer mock = mock(NorgConsumer.class);

        when(mock.finnEnhetForGeografiskTilknytning(any(String.class)))
                .thenAnswer(NorgConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }

    public static RsNorgEnhet getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock){

        Map<String, RsNorgEnhet> rsNorgEnhetMap = responses.get(SubjectHandler.getUserIdFromToken());
        if (rsNorgEnhetMap == null){
            rsNorgEnhetMap = getDefaultMap();
            responses.put(SubjectHandler.getUserIdFromToken(), rsNorgEnhetMap);
        }

        String argumentAt = invocationOnMock.getArgumentAt(0, String.class);
        return rsNorgEnhetMap.get(argumentAt);
    }


    public static RsNorgEnhet getKommuneResponse(String enhetsnavn, String enhetsNr){
        return new RsNorgEnhet()
                .withEnhetId(Long.parseLong("9"+ enhetsNr + enhetsNr))
                .withEnhetNr(enhetsNr)
                .withNavn("NAV " + enhetsnavn)
                .withOrgNrTilKommunaltNavKontor("9"+ enhetsNr + enhetsNr);
    }

    private static Map<String, RsNorgEnhet> getDefaultMap(){
        return new ImmutableMap.Builder<String, RsNorgEnhet>()
                .put("120102", new RsNorgEnhet().withEnhetId(100000250)
                        .withEnhetNr("1209")
                        .withNavn("NAV Bergenhus")
                        .withOrgNrTilKommunaltNavKontor("976830563"))
                .put("0106", getKommuneResponse("Fredrikstad", "0106"))
                .put("0701", getKommuneResponse("Horten", "0701"))
                .put("0101", getKommuneResponse("Halden", "0101"))
                .put("1247", getKommuneResponse("Askøy", "1247"))
                .put("0105", getKommuneResponse("Sarpsborg", "0105"))
                .put("0219", getKommuneResponse("Bærum", "0219"))
                .put("0111", getKommuneResponse("Hvaler", "0111"))
                .put("5001", getKommuneResponse("Moss", "5001"))
                .put("0136", getKommuneResponse("Rygge", "0136"))
                .put("0403", getKommuneResponse("Hamar", "0403"))
                .put("2222", getKommuneResponse("Dobbelby", "2222"))
                .put("2223", getKommuneResponse("Dobbelby2", "2223"))
                .build();
    }

    public static void setNorgMap(String rsNorgEnhetMap){
        try {
            ObjectMapper mapper = new ObjectMapper();

            TypeReference<HashMap<String, RsNorgEnhet>> typeRef
                    = new TypeReference<HashMap<String, RsNorgEnhet>>() {};

            Map<String, RsNorgEnhet> map = mapper.readValue(rsNorgEnhetMap, typeRef);

            Map<String, RsNorgEnhet> response = responses.get(SubjectHandler.getUserIdFromToken());
            if (response == null){
                responses.put(SubjectHandler.getUserIdFromToken(), map);
            } else {
                responses.replace(SubjectHandler.getUserIdFromToken(), map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetNorgMap(){
        responses.replace(SubjectHandler.getUserIdFromToken(), getDefaultMap());
    }
}
