package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.InformasjonRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.TestThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid.ArbeidssokerInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;
import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursUtenOidcTest {


    @Spy
    InformasjonService informasjonService;
    @Spy
    SoknadService soknadService;
    @Spy
    LandService landService;
    @Mock
    PersonaliaFletter personaliaFletter;
    @Mock
    PersonInfoService personInfoService;
    @Mock
    TestThreadLocalSubjectHandler subjectHandler;
    @Mock
    NavMessageSource messageSource;
    @Mock
    WebSoknadConfig soknadConfig;
    @Mock
    ArbeidssokerInfoService arbeidssokerInfoService;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;


    @InjectMocks
    InformasjonRessurs ressurs = spy(new InformasjonRessurs());

    @InjectMocks
    InformasjonRessursTest ressursTest;

    @Before
    public void setUp() {
        ressursTest.setUp();
        System.setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    // Duplisert kun denne testen, da det er den eneste som berører funksjonalitet med subjectHandler.
    @Test
    public void utslagskriterierInneholderAlleKriteriene() {
        ressursTest.utslagskriterierInneholderAlleKriteriene();
    }
}
