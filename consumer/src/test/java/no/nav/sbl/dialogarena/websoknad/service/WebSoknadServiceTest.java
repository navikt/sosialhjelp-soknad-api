package no.nav.sbl.dialogarena.websoknad.service;

import java.util.ArrayList;
import java.util.List;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import javax.inject.Named;

@RunWith(value = MockitoJUnitRunner.class)
public class WebSoknadServiceTest {
    
    private static final String BEHANDLINGS_ID = "129187212";
	@Mock
    @Named("sendSoknadService")
    SendSoknadPortType webservice;
	
	@InjectMocks
	HenvendelseConnector service;
	
	Long soknadId;

	@Before
	public void setUp() {
		
	}
	
	@Test
	public void skalKunneStarteSoknad() {
	    when(webservice.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(lagResultatFraStartSoknad());
		String behandlingsId = service.startSoknad("***REMOVED***", lagTomFaktaListe());
		assertThat(behandlingsId, equalTo(BEHANDLINGS_ID));
	}

    private WSBehandlingsId lagResultatFraStartSoknad() {
        return new WSBehandlingsId().withBehandlingsId(BEHANDLINGS_ID);
    }
    
    private List<Faktum> lagTomFaktaListe() {
        return new ArrayList<Faktum>();
    }
	
}
