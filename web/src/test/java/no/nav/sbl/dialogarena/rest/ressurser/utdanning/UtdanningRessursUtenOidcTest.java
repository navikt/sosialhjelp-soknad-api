package no.nav.sbl.dialogarena.rest.ressurser.utdanning;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtdanningRessursUtenOidcTest {

    private static final String BEHANDLINGSID = "123";

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private TextService textService;

    @InjectMocks
    private UtdanningRessurs utdanningRessurs = spy(new UtdanningRessurs());

    @InjectMocks
    private UtdanningRessursTest utdanningRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningUtenErStudentOgStudentgrad(){
        utdanningRessursTest.getUtdanningSkalReturnereUtdanningUtenErStudentOgStudentgrad();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErIkkeStudent(){
        utdanningRessursTest.getUtdanningSkalReturnereUtdanningMedErIkkeStudent();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudent(){
        utdanningRessursTest.getUtdanningSkalReturnereUtdanningMedErStudent();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradHeltid(){
        utdanningRessursTest.getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradHeltid();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradDeltid(){
        utdanningRessursTest.getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradDeltid();
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudent(){
        utdanningRessursTest.putUtdanningSkalSetteUtdanningMedErStudent();
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudentOgStudentgrad(){
        utdanningRessursTest.putUtdanningSkalSetteUtdanningMedErStudentOgStudentgrad();
    }
}
