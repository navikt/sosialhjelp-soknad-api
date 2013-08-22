package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import fit.Fixture;
import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.test.fitnesse.fixture.ToDoList;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.SetupDokumentServiceIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class OpplastingsvinduFixture extends SpringAwareDoFixture {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Inject
    private DokumentServiceMock dokumentServiceIntegrationMock;

    @Before
    public void resetDokumentServiceIntegrationMock() {
        dokumentServiceIntegrationMock.reset();
    }

    public Fixture datagrunnlag() {
        return new SetupDokumentServiceIntegration(dokumentServiceIntegrationMock);
    }

    public Fixture visModalVindu() {
        return new SjekkVisOpplastingsvindu(wicketTester);
    }

    public Fixture sjekkFilformaterOgStorrelse() {
        return new SjekkFilformaterOgStorrelse(wicketTester);
    }

    public ToDoList avklaringer() {
        return new ToDoList();
    }
    
}
