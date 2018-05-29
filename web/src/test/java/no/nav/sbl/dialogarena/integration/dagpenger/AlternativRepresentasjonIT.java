package no.nav.sbl.dialogarena.integration.dagpenger;

import no.nav.sbl.dialogarena.integration.AbstractIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonIT extends AbstractIT {

    private String dagPengerSkjemanummer = new DagpengerOrdinaerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    @Test
    public void henterAlternativJsonRepresentasjon() {
        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(dagPengerSkjemanummer).settDelstegstatus("opprettet")
                .nyttFaktum("utslagskriterier.dagpenger.utbetaling").withValue("nei").opprett()
                .nyttFaktum("reellarbeidssoker.villigdeltid").withValue("true").opprett();

        Response response = testSoknad.hentAlternativJSONRepresentasjonResponseMedStatus();
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(200);
    }







}
