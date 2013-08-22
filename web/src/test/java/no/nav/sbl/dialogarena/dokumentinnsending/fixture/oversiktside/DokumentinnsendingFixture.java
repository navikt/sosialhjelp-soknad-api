package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import fit.Fixture;
import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.test.fitnesse.fixture.ToDoList;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.SetupDokumentServiceIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu.OpplastingAvVedlegg;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu.SjekkFilerSendtTilHenvendelse;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class DokumentinnsendingFixture extends SpringAwareDoFixture {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Inject
    private DokumentServiceMock dokumentServiceMock;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FortsettSenereFixture fortsettSenereFixture;
    @Inject
    private NavigasjonTestFixture navigasjonTest;

    @Before
    public void resetDokumentServiceIntegrationMock() {
        dokumentServiceMock.reset();
    }

    public Fixture datagrunnlag() {
        return new SetupDokumentServiceIntegration(dokumentServiceMock);
    }

    public Fixture visDokumentoversikt() {
        return new SjekkVisDokumentoversikt(wicketTester);
    }

    public Fixture vedleggForBrukerbehandling(String behandlingsId) {
        return new SjekkVedleggsoversiktForBrukerbehandling(behandlingsId, wicketTester);
    }

    public Fixture vedleggForBrukerbehandlingSomSkalBekreftes(String behandlingsId) {
        return new SjekkBekreftingAvVedleggFoerInnsending(behandlingsId, wicketTester);
    }

    public ToDoList avklaringer() {
        return new ToDoList();
    }

    public Fixture opplastingAvVedleggTilSuF8knad() {
        return new OpplastingAvVedlegg(wicketTester, soknadService);
    }

    public Fixture sletteVedlegg() {
        return new SlettVedleggFixture(wicketTester, soknadService);
    }

    public Fixture filerOversendtTilHenvendelse() {
        return new SjekkFilerSendtTilHenvendelse(dokumentServiceMock);
    }

    public Fixture leggeTilVedleggslinje() {
        return new SjekkLeggTilVedleggslinje(wicketTester);
    }

    public Fixture endreVedleggslinje() {
        return new SjekkEndreVedleggslinje(wicketTester);
    }

    public Fixture sletteSoknad() {
        return new SjekkSlettSoknad(wicketTester);
    }

    public Fixture seOppdatertOversikt() {
        return new SjekkOppdaterOversikt(wicketTester);
    }

    public Fixture sendtTilNAV() {
        return new SendtTilNav(dokumentServiceMock, wicketTester);
    }

    public Fixture fortsettSenere() {
        return fortsettSenereFixture;
    }

    public Fixture navigasjonTest() {
        return navigasjonTest;
    }
}
