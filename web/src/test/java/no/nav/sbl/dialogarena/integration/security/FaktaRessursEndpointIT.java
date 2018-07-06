package no.nav.sbl.dialogarena.integration.security;


import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;

public class FaktaRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgang_hentFaktum() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
            Response response = soknadTester.sendsoknadResource("fakta/1", webTarget -> webTarget
                    .queryParam("fnr", ANNEN_BRUKER)) //fake annen bruker, se FakeLoginFilter
                    .buildGet()
                    .invoke();
            assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void nektetTilgangUtenToken_opprettFaktum() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        Response response = soknadTester.sendsoknadResource("fakta", webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER) //fake annen bruker, se FakeLoginFilter
                .queryParam("behandlingsId", soknadTester.getBrukerBehandlingId()))
                .buildPost(Entity.json(faktum()))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    private static final Faktum faktum() {
        Faktum faktum = new Faktum();
        faktum.setKey("annetfaktum");
        faktum.setValue("Test");
        return faktum;
    }

}
