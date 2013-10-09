package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadDataOppsummering;

import org.junit.Assert;
import org.junit.Test;

public class TransformersTest {

	@Test
	public void skalKunneTransformereWSDataTilFaktum() {
		Long soknadId = 1l;
		WSBrukerData wsBrukerData = new WSBrukerData();
		wsBrukerData.setNokkel("nokkel");
		wsBrukerData.setVerdi("verdi");
		wsBrukerData.setType("type");
		
		Faktum faktum = Transformers.tilFaktum(soknadId).transform(wsBrukerData);
		
		Assert.assertEquals("nokkel", faktum.getKey());
		Assert.assertEquals("verdi", faktum.getValue());
		Assert.assertEquals("type", faktum.getType());
		Assert.assertEquals(soknadId, faktum.getSoknadId());
	}
	
	@Test
	public void skalKunneTransformereStatus () {
		WSSoknadDataOppsummering wsOppsummering = new WSSoknadDataOppsummering();
		wsOppsummering.setStatus("startet");
		String status = Transformers.TIL_STATUS.transform(wsOppsummering);
		
		Assert.assertEquals("startet", status);
	}
	
	@Test
	public void skalKunneTransformereSoknadId () {
		WSSoknadDataOppsummering wsOppsummering = new WSSoknadDataOppsummering();
		wsOppsummering.setSoknadId(3l);
		Long soknadId = Transformers.TIL_SOKNADID.transform(wsOppsummering);
		
		Assert.assertEquals(new Long(3l), soknadId);
	}
	
}
