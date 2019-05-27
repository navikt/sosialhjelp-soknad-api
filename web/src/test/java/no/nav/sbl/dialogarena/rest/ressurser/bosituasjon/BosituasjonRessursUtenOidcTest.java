package no.nav.sbl.dialogarena.rest.ressurser.bosituasjon;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
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
public class BosituasjonRessursUtenOidcTest {

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

    @InjectMocks
    private BosituasjonRessurs bosituasjonRessurs = spy(new BosituasjonRessurs());

    @InjectMocks
    private BosituasjonRessursTest bosituasjonRessursTest;

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
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersonerLikNull(){
        bosituasjonRessursTest.getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersonerLikNull();
    }

    @Test
    public void getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersoner(){
        bosituasjonRessursTest.getBosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersoner();
    }

    @Test
    public void putBosituasjonSkalSetteBosituasjon(){
        bosituasjonRessursTest.putBosituasjonSkalSetteBosituasjon();
    }

    @Test
    public void putBosituasjonSkalSetteAntallPersonerLikNull(){
        bosituasjonRessursTest.putBosituasjonSkalSetteAntallPersonerLikNull();
    }
}
