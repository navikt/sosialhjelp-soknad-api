package no.nav.sbl.dialogarena;

import static no.nav.sbl.dialogarena.websoknad.service.Transformers.tilFaktum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSoknadServiceMock implements SendSoknadService{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(WebSoknadServiceMock.class);
	
	@Inject
	private SoknadInnsendingRepository repository;
	
	@Override
	public WebSoknad hentSoknad(long soknadId) {
		return repository.hentSoknad(soknadId);
	}

	@Override
	public void lagreSoknadsFelt(long soknadId, String key, String value) {
		repository.lagre(soknadId, key, value);
		
	}

	@Override
	public void sendSoknad(long soknadId) {
		repository.sendSoknad(soknadId);
		
	}

	@Override
	public List<Long> hentMineSoknader(String aktorId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void avbrytSoknad(Long soknadId) {
		repository.slettSoknad(soknadId);
	}

	

	public Long startSoknad(String navSoknadId) {
		return repository.startSoknad(navSoknadId);
	}

	private WebSoknad convertToSoknad(WSSoknadData wsSoknad) {
		Long soknadId = wsSoknad.getSoknadId();
		Map<String, Faktum> fakta = new LinkedHashMap<>();
		for (WSBrukerData wsBrukerData : wsSoknad.getFaktum()) {
			fakta.put(wsBrukerData.getNokkel(),
					tilFaktum(soknadId).transform(wsBrukerData));
		}

		WebSoknad soknad = new WebSoknad();
		soknad.setSoknadId(soknadId);
		soknad.setBrukerBehandlingId(wsSoknad.getBrukerBehandlingId());
		soknad.setGosysId(wsSoknad.getGosysId());
		soknad.leggTilFakta(fakta);

		return soknad;
	}

}
