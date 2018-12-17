package no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganisasjonMock {

    private static Map<String, HentOrganisasjonResponse> responses = new HashMap<>();

    public OrganisasjonV4 organisasjonMock(){

        final OrganisasjonV4 mock = mock(OrganisasjonV4.class);

        try {
            when(mock.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
                    .thenAnswer(invocationOnMock -> getOrCreateCurrentUserResponse());
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput err) {
            err.printStackTrace();
        }
        return mock;

    }

    private static HentOrganisasjonResponse getOrCreateCurrentUserResponse(){

        HentOrganisasjonResponse response = responses.get(SubjectHandler.getSubjectHandler().getUid());
        if (response == null ){
            response = getDefaultResponse();
            responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
        }

        return response;
    }

    private static HentOrganisasjonResponse getDefaultResponse(){
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();

        return response;
    }

    public static void setOrganisasjon(String  jsonOrganisasjon){

        try {
            ObjectMapper mapper = new ObjectMapper();
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(SammensattNavn.class, new SammensattNavnDeserializer());
            module.addDeserializer(Organisasjon.class, new OrganisasjonDeserializer());
            mapper.registerModule(module);
            HentOrganisasjonResponse response = mapper.readValue(jsonOrganisasjon, HentOrganisasjonResponse.class);
            if (responses.get(SubjectHandler.getSubjectHandler().getUid()) == null){
                responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
            } else {
                responses.replace(SubjectHandler.getSubjectHandler().getUid(), response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void slettOrganisasjon(){
        responses.replace(SubjectHandler.getSubjectHandler().getUid(), new HentOrganisasjonResponse());
    }


}
