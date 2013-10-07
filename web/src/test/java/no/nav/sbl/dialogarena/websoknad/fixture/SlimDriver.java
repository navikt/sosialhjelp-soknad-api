package no.nav.sbl.dialogarena.websoknad.fixture;

import javax.inject.Inject;

import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.sbl.dialogarena.websoknad.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknadId;
import no.nav.sbl.dialogarena.websoknad.servlet.SoknadDataController;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class SlimDriver extends SpringAwareDoFixture {

	@Inject
	private SoknadDataController soknadDataController;

	private WebSoknadId webSoknadId;

	private WebSoknad webSoknad;
	
	public SlimDriver(String fnr) throws Exception{
		super.setUp();
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
	
	public boolean erSoknadSlettet(long soknadId) {
		WebSoknad soknad = soknadDataController.hentSoknadData(soknadId);
		return soknad == null;
		
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
		webSoknad.getFakta().put(faktum, new Faktum(webSoknad.getSoknadId(), faktum, verdi, "BRUKER"));
		soknadDataController.lagreSoknad(webSoknadId.getId(),webSoknad);
	}
	
	
}
