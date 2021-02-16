package no.nav.sosialhjelp.soknad.mock.norg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import no.nav.sosialhjelp.soknad.domain.model.norg.NorgConsumer;
import no.nav.sosialhjelp.soknad.domain.model.norg.NorgConsumer.RsNorgEnhet;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NorgConsumerMock {

    private static Map<String, Map<String, RsNorgEnhet>> responses = new HashMap<>();

    public NorgConsumer norgConsumerMock(){
        NorgConsumer mock = mock(NorgConsumer.class);

        when(mock.getEnhetForGeografiskTilknytning(any(String.class)))
                .thenAnswer(NorgConsumerMock::getOrCreateCurrentUserResponse);

        return mock;
    }

    public static RsNorgEnhet getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock){

        Map<String, RsNorgEnhet> norgEnhetMap = responses.get(SubjectHandler.getUserId());
        if (norgEnhetMap == null){
            norgEnhetMap = getDefaultMap();
            responses.put(SubjectHandler.getUserId(), norgEnhetMap);
        }

        String grafiskTilknytning = invocationOnMock.getArgument(0);
        return norgEnhetMap.get(grafiskTilknytning);
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
                .put("030102", new RsNorgEnhet().withEnhetId(100000250)
                        .withEnhetNr("0315")
                        .withNavn("NAV Grünerløkka")
                        .withOrgNrTilKommunaltNavKontor("811213322"))
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
                .put("4614", getKommuneResponse("Stord", "4614"))
                .build();
    }

    public static void setNorgMap(String rsNorgEnhetMap){
        try {
            ObjectMapper mapper = new ObjectMapper();

            TypeReference<HashMap<String, RsNorgEnhet>> typeRef
                    = new TypeReference<HashMap<String, RsNorgEnhet>>() {};

            Map<String, RsNorgEnhet> map = mapper.readValue(rsNorgEnhetMap, typeRef);

            Map<String, RsNorgEnhet> response = responses.get(SubjectHandler.getUserId());
            if (response == null){
                responses.put(SubjectHandler.getUserId(), map);
            } else {
                responses.replace(SubjectHandler.getUserId(), map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetNorgMap(){
        responses.replace(SubjectHandler.getUserId(), getDefaultMap());
    }
}
