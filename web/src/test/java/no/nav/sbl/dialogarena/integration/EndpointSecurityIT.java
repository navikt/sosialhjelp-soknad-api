package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointSecurityIT extends AbstractIT {
    private String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalIkkeHaTilgangTilAndresFakta() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        try{
            soknadTester.somBruker("12345679811").hentFakta();
        }catch (WebApplicationException e){
            assertThat(e.getResponse().getStatusInfo()).isEqualToComparingFieldByField(Response.Status.FORBIDDEN);
        }



    }





}
