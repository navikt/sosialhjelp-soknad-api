package no.nav.sbl.dialogarena.websoknad.fixture;

import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.core.context.SubjectHandlerUtils;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknadId;
import no.nav.sbl.dialogarena.websoknad.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.websoknad.servlet.SoknadDataController;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class SoknadInnsendingDriver extends SpringAwareDoFixture {

	static { 
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		System.setProperty(ModigSecurityConstants.SYSTEMUSER_USERNAME, "BD05");
	}
	
	@Inject
	private SoknadDataController soknadDataController;

	private WebSoknadId webSoknadId;

	private WebSoknad webSoknad;

	private String fnr;
	
	
	
	public SoknadInnsendingDriver(String fnr) throws Exception{
		super.setUp();
		this.fnr = fnr;
		SubjectHandlerUtils.setEksternBruker(fnr, 4, null);
	}
	
	public String getFnr() {
		return fnr;
	}
	
	public long opprettNySoknad(String type)  {
		this.webSoknadId = startSoknad();
		return webSoknadId.getId();
	}
	
	public boolean soknadOpprettet() {
		return webSoknadId != null && webSoknadId.getId() > 0;
	}
	
	public long hentSoknadId() {
		return webSoknadId.getId();
	}
	
	public void avbrytSoknad(long soknadId) {
		soknadDataController.slettSoknad(soknadId);
	}
	
	public void hentSoknad(long soknadId) {
		this.webSoknad = soknadDataController.hentSoknadData(soknadId);
	}
	
	public void sendSoknad(long soknadId) {
		soknadDataController.sendSoknad(soknadId);
	}
	
	public String soknadStatus(long soknadId) {
		WebSoknad soknad = soknadDataController.hentSoknadData(soknadId);
		return soknad.getStatus().name();
		
	}
	public long antallFaktumLagret() {
		if (webSoknad != null) {
			return webSoknad.antallFakta();
		} else {
			return 0;
		}
	}

	public String listFakta() {
		return webSoknad.getFakta().toString();
	}
	private WebSoknadId startSoknad() {
		return soknadDataController.opprettSoknad("Dagpenger");
	}
	
	public void lagreFaktumMedVerdi(String faktum, String verdi) {
		webSoknad.getFakta().put(faktum, new Faktum(webSoknad.getSoknadId(), null, faktum, verdi, "BRUKER"));
		soknadDataController.lagreSoknad(webSoknadId.getId(),webSoknad);
	}
	
	
}
