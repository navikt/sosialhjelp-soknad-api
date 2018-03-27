package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FaktaRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgang_opprettFaktum() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .somBruker(ANNEN_BRUKER);
        try{
            soknadTester.nyttFaktum("annenpersonsfaktum").opprett();
            fail("Fikk ikke exception ved ulovlig aksess");
        }catch (WebApplicationException e){
            assertForbiddenStatus(e);
        }
    }

    @Ignore("Feiler med feil xsrf-token, ikke med feil bruker")
    @Test
    public void nektetTilgang_lagreFaktum() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .somBruker(ANNEN_BRUKER);
        try{
            soknadTester.faktum("land").withValue("NOR").utforEndring();
            fail("Fikk ikke exception ved ulovlig aksess");
        }catch (WebApplicationException e){
            assertForbiddenStatus(e);
        }
    }

    private void assertForbiddenStatus(WebApplicationException e) {
        assertThat(e.getResponse().getStatusInfo()).isEqualToComparingFieldByField(Response.Status.FORBIDDEN);
    }





}
