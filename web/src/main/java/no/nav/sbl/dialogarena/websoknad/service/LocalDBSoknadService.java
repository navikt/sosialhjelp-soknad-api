package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepository;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

@ComponentScan()
public class LocalDBSoknadService implements SendSoknadService{
	
	private static final Logger logger = LoggerFactory.getLogger(LocalDBSoknadService.class);
	
	private static final String BRUKERREGISTRERT_FAKTUM = "BRUKERREGISTRERT";
	
//	@Inject
//    @Named("sendSoknadService")
//    private SendSoknadPortType sendSoknadService;
	
	@Inject
	@Named("soknadInnsendingRepository")
	private SoknadRepository repository;
	
	@Override
	public WebSoknad hentSoknad(long soknadId) {
		return repository.hentSoknadMedData(soknadId);
	}

	@Override
	public void lagreSoknadsFelt(long soknadId, String key, String value) {
		repository.lagreFaktum(soknadId, new Faktum(soknadId, key, value, BRUKERREGISTRERT_FAKTUM));
		
	}

	@Override
	public void sendSoknad(long soknadId) {
		repository.avslutt(new WebSoknad().medId(soknadId));
		
	}

	@Override
	public List<Long> hentMineSoknader(String aktorId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void avbrytSoknad(Long soknadId) {
		//TODO: Refaktorerer. Trenger bare å sende id
		repository.avbryt(new WebSoknad().medId(soknadId));
	}

	

	public Long startSoknad(String navSoknadId) {
		logger.debug("Starter ny søknad");
		//TODO: Sende et signal til Henvendelse om at søknaden er startet
        String behandlingsId = UUID.randomUUID().toString();
        logger.debug("Start søknad");
        
        //TODO-KJ: Denne operasjonen er ikke klar enda.Vil kommenteres inn når den er
//        try {
//            behandlingsId = sendSoknadService.startBehandling(navSoknadId);
//        } catch (SOAPFaultException e) {
//            logger.error("Feil ved oppretting av søknad med ID", navSoknadId, e);
//            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
//        }
//        
        WebSoknad soknad = WebSoknad.startSoknad().
        		medBehandlingId(behandlingsId).
        		medGosysId(navSoknadId).
        		medAktorId(SubjectHandler.getSubjectHandler().getUid()).
        		opprettetDato(DateTime.now());
		return repository.opprettSoknad(soknad);
	}
}
