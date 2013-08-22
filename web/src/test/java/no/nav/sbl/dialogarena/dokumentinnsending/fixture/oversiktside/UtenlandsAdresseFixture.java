package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import fit.Fixture;
import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.SetupBrukerprofilIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class UtenlandsAdresseFixture extends SpringAwareDoFixture{

	 @Inject
	 private FluentWicketTester<WicketApplication> wicketTester;
	 
	 @Inject 
	 PersonServiceMock personService;
	 
	 @Inject
	 DokumentServiceMock dokumentService;
	 
	 public Fixture datagrunnlag() {
		 return new SetupBrukerprofilIntegration(personService, dokumentService);
	 }
	 
	 public Fixture informasjonOmUtenlandsAdresse() {
		 return new InformasjonOmUtenlandsAdresse(wicketTester, dokumentService);
	 }
}
