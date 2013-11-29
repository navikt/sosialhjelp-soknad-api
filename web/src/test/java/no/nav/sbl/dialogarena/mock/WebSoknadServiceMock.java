package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.SoknadInnsendingRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

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
	public Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum) {
		Long faktumId = repository.lagre(soknadId, faktum.getKey(), faktum.getValue());
		return repository.hentFaktum(soknadId, faktumId);
		
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

	@Override
	public Faktum lagreSystemSoknadsFelt(Long soknadId, Faktum faktum) {
		// TODO Auto-generated method stub
		return null;
	}

}
