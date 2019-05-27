package no.nav.sbl.dialogarena.sikkerhet;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.NotFoundException;

import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class SikkerhetsAspectUtenOidcTest {

    @Mock
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private FaktaService faktaService;
    @InjectMocks
    private SikkerhetsAspect sikkerhetsAspect = spy(new SikkerhetsAspect());

    @InjectMocks
    private SikkerhetsAspectTest sikkerhetsAspectTest;

    private String brukerBehandlingsId = "1";

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void skalSjekkeOmBrukerHarTilgangTilFakta() {
        sikkerhetsAspectTest.skalSjekkeOmBrukerHarTilgangTilFakta();
    }

    @Test(expected = NotFoundException.class)
    public void skalGiNotFoundExceptionOmRessursIkkeFinnes() {
        sikkerhetsAspectTest.skalGiNotFoundExceptionOmRessursIkkeFinnes();
    }

    @Test
    public void skalSjekkeOmBrukerHarTilgangTilVedlegg() {
        sikkerhetsAspectTest.skalSjekkeOmBrukerHarTilgangTilVedlegg();
    }

    @Test(expected = NotFoundException.class)
    public void skalHandtereHvisIkkeVedleggFinnes() {
        sikkerhetsAspectTest.skalHandtereHvisIkkeVedleggFinnes();
    }

    @Test(expected = AuthorizationException.class)
    public void skalKasteExceptionNaarTokenIkkeStemmer() {
        sikkerhetsAspectTest.skalKasteExceptionNaarTokenIkkeStemmer();
    }

    @Test
    public void soknadRessursSkalBliMatchetAvAspectet() {
        sikkerhetsAspectTest.soknadRessursSkalBliMatchetAvAspectet();
    }
}
