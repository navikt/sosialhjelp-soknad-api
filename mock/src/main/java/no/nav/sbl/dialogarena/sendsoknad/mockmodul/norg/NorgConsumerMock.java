package no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
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
                .thenAnswer(invocationOnMock -> getOrCreateCurrentUserResponse(invocationOnMock));

        return mock;
    }

    public static RsNorgEnhet getOrCreateCurrentUserResponse(InvocationOnMock invocationOnMock){

        Map<String, RsNorgEnhet> rsNorgEnhetMap = responses.get(OidcFeatureToggleUtils.getUserId());
        if (rsNorgEnhetMap == null){
            rsNorgEnhetMap = getDefaultMap();
            responses.put(OidcFeatureToggleUtils.getUserId(), rsNorgEnhetMap);
        }
        
        String argumentAt = invocationOnMock.getArgumentAt(0, String.class);
        return rsNorgEnhetMap.get(argumentAt);
    }

    private static Map<String, RsNorgEnhet> getDefaultMap(){
        return new ImmutableMap.Builder<String, RsNorgEnhet>()
                .put("120102", new RsNorgEnhet().withEnhetId(100000250)
                        .withEnhetNr("1209")
                        .withNavn("NAV Bergenhus")
                        .withOrgNrTilKommunaltNavKontor("976830563"))
                .build();
    }

    public static void setNorgMap(String rsNorgEnhetMap){
        try {
            ObjectMapper mapper = new ObjectMapper();

            TypeReference<HashMap<String, RsNorgEnhet>> typeRef
                    = new TypeReference<HashMap<String, RsNorgEnhet>>() {};

            Map<String, RsNorgEnhet> map = mapper.readValue(rsNorgEnhetMap, typeRef);

            Map<String, RsNorgEnhet> response = responses.get(OidcFeatureToggleUtils.getUserId());
            if (response == null){
                responses.put(OidcFeatureToggleUtils.getUserId(), map);
            } else {
                responses.replace(OidcFeatureToggleUtils.getUserId(), map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetNorgMap(){
        responses.replace(OidcFeatureToggleUtils.getUserId(), getDefaultMap());
    }
}
