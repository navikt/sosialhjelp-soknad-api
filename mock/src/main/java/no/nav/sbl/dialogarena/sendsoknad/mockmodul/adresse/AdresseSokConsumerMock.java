package no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.modig.core.context.SubjectHandler;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;

public class AdresseSokConsumerMock {

    private static Map<String, AdressesokRespons> responses = new HashMap<>();

    public AdresseSokConsumer adresseRestService() {
        AdresseSokConsumer mock = mock(AdresseSokConsumer.class);

        when(mock.sokAdresse(any(Sokedata.class))).thenAnswer((invocation) -> getOrCreateCurrentUserResponse());
        when(mock.sokAdresse(anyString())).thenAnswer((invocation) -> getOrCreateCurrentUserResponse());

        return mock;
    }

    private static AdressesokRespons getOrCreateCurrentUserResponse() {
        AdressesokRespons response = responses.get(SubjectHandler.getSubjectHandler().getUid());
        if (response == null){
            response = getDefaultRespons();
            responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
        }

        return response;
    }

    private static AdressesokRespons getDefaultRespons(){
        AdressesokRespons response = new AdressesokRespons();

        return response;
    }

    public static void setAdresser(String jsonAdressesokRespons){

        try {
            ObjectMapper mapper = new ObjectMapper();
            AdressesokRespons response = mapper.readValue(jsonAdressesokRespons, AdressesokRespons.class);
            if (responses.get(SubjectHandler.getSubjectHandler().getUid()) == null){
                responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
            } else {
                responses.replace(SubjectHandler.getSubjectHandler().getUid(), response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void settDefaultAdresser(){
        AdressesokRespons defaultRespons = new AdressesokRespons();
        responses.replace(SubjectHandler.getSubjectHandler().getUid(), defaultRespons);
    }
}
