package no.nav.sbl.dialogarena.websoknad.service;


import static org.mockito.Mockito.*;

import javax.inject.Named;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.remoting.soap.SoapFaultException;

@RunWith(value = MockitoJUnitRunner.class)
public class WebSoknadServiceTest {
	
	@Mock
    @Named("sendSoknadService")
    SendSoknadPortType sendSoknadService;
	
	@InjectMocks
	WebSoknadService service;
	
	
	Long soknadId;
	@Before
	public void setUp() {
		soknadId = service.startSoknad("dagpenger");
		WSSoknadData wsSoknadData = new WSSoknadData();
		wsSoknadData.setSoknadId(soknadId);
		
		when(sendSoknadService.hentSoknad(soknadId)).thenReturn(wsSoknadData);
	}
	
	@Test
	public void skalKunneStarteSoknad() {
		Assert.assertNotNull(soknadId);
	}
	
	@Test
	public void skalKunneLeggeTilFaktum() {
		Long soknadId = service.startSoknad("dagpenger");
		
		WebSoknad soknad = service.hentSoknad(soknadId);
		soknad.leggTilFaktum("enKey", new Faktum(soknadId, "enKey", "enVerdi", null));
		
		Assert.assertEquals(1, soknad.antallFakta());
		Assert.assertEquals("enVerdi", soknad.getFakta().get("enKey").getValue());
	}
	
	@Test
	public void skalKunneAvbryteSoknad() {
		service.avbrytSoknad(soknadId);
		verify(sendSoknadService, times(1)).avbrytSoknad(soknadId);
	}
	
	@Test
	public void skalKunneSendeSoknad() {
		service.sendSoknad(soknadId);
		verify(sendSoknadService, times(1)).sendSoknad(soknadId);
	}
	
	@Test
	public void skalKunneLagreFelt() {
		service.lagreSoknadsFelt(soknadId, "enKey", "enValue");
		verify(sendSoknadService, times(1)).lagreBrukerData(soknadId, "enKey", "enValue");
	}
}
