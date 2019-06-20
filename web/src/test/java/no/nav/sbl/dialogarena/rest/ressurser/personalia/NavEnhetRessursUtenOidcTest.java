package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class NavEnhetRessursUtenOidcTest {

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadsmottakerService soknadsmottakerService;

    @Mock
    private NorgService norgService;

    @InjectMocks
    private NavEnhetRessurs navEnhetRessurs = spy(new NavEnhetRessurs());

    @InjectMocks
    private NavEnhetRessursTest navEnhetRessursTest;

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
    public void getNavEnheterSkalReturnereEnheterRiktigKonvertert(){
        navEnhetRessursTest.getNavEnheterSkalReturnereEnheterRiktigKonvertert();
    }

    @Test
    public void getNavEnheterSkalReturnereTomListeNaarOppholdsadresseIkkeErValgt(){
        navEnhetRessursTest.getNavEnheterSkalReturnereTomListeNaarOppholdsadresseIkkeErValgt();
    }

    @Test
    public void putNavEnhetSkalSetteNavenhet(){
        navEnhetRessursTest.putNavEnhetSkalSetteNavenhet();
    }
}