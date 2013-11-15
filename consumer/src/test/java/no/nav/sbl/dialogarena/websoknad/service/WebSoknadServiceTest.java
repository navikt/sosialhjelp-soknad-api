package no.nav.sbl.dialogarena.websoknad.service;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(value = MockitoJUnitRunner.class)
public class WebSoknadServiceTest {
	@Mock
    @Named("sendSoknadService")
    SendSoknadPortType webservice;
	
	@InjectMocks
	WebSoknadService service;
	
	
	Long soknadId;

	@Before
	public void setUp() {
		soknadId = service.startSoknad("dagpenger");
		WSSoknadData wsSoknadData = new WSSoknadData();
		wsSoknadData.setSoknadId(soknadId);
		
		when(webservice.hentSoknad(soknadId)).thenReturn(wsSoknadData);
	}
	
	@Test
	public void skalKunneStarteSoknad() {
		Assert.assertNotNull(soknadId);
	}

	@Test(expected = ApplicationException.class)
	public void skalFaaApplicationExceptionVedStartDersomNoeErFeil() {
		when(webservice.startSoknad("-1")).thenThrow(SOAPFaultException.class);
		service.startSoknad("-1");
	}
	
	@Test
	public void skalKunneLeggeTilFaktum() {
		WebSoknad soknad = service.hentSoknad(soknadId);
		soknad.leggTilFaktum("enKey", new Faktum(soknadId, "enKey", "enVerdi", null));
		
		Assert.assertEquals(1, soknad.antallFakta());
		Assert.assertEquals("enVerdi", soknad.getFakta().get("enKey").getValue());
	}
	
	@Test(expected = ApplicationException.class)
	public void skalFaaApplicationExceptionVedHentingDersomNoeErFeil() {
		when(webservice.hentSoknad(-1l)).thenThrow(SOAPFaultException.class);
		service.hentSoknad(-1l);
	}
	
	@Test
	public void skalKunneAvbryteSoknad() {
		service.avbrytSoknad(soknadId);
		verify(webservice, times(1)).avbrytSoknad(soknadId);
	}
	
	@Test
	public void skalKunneSendeSoknad() {
		service.sendSoknad(soknadId);
		verify(webservice, times(1)).sendSoknad(soknadId);
	}
	
	@Test
	public void skalKunneLagreFelt() {
		service.lagreSoknadsFelt(soknadId, "enKey", "enValue");
		verify(webservice, times(1)).lagreBrukerData(soknadId, "enKey", "enValue");
	}

    @Test
    public void testTransform(){


    }

}
