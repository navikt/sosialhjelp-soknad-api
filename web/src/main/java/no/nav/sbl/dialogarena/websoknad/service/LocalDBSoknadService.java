package no.nav.sbl.dialogarena.websoknad.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;


import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepository;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDBSoknadService implements SendSoknadService{
	
	private static final Logger logger = LoggerFactory.getLogger(LocalDBSoknadService.class);
	
	@Inject
    @Named("sendSoknadService")
    private SendSoknadPortType sendSoknadService;
	
	@Inject
	@Named("soknadInnsendingRepository")
	private SoknadRepository repository;
	
	@Override
	public WebSoknad hentSoknad(long soknadId) {
		return repository.hentSoknadMedData(soknadId);
	}



	@Override
	public void lagreSoknadsFelt(long soknadId, String key, String value) {
		repository.lagreFaktum(soknadId, new Faktum(soknadId, key, value));
		
	}

	@Override
	public void sendSoknad(long soknadId) {
		//repository.sendSoknad(soknadId);
		
	}

	@Override
	public List<Long> hentMineSoknader(String aktorId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void avbrytSoknad(Long soknadId) {
		//repository.slettSoknad(soknadId);
	}

	

	public Long startSoknad(String navSoknadId) {
		//TODO: Sende et signal til Henvendelse om at søknaden er startet
        
        WebSoknad soknad = WebSoknad.startSoknad().medGosysId(navSoknadId).medAktorId(SubjectHandler.getSubjectHandler().getUid()).opprettetDato(DateTime.now());
		return repository.opprettSoknad(soknad);
	}
}
