package no.nav.sbl.dialogarena.rest.ressurser.begrunnelse;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class BegrunnelseRessursUtenOidcTest {

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private BegrunnelseRessurs begrunnelseRessurs = spy(new BegrunnelseRessurs());

    @InjectMocks
    private BegrunnelseRessursTest begrunnelseRessursTest;

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
    public void getBegrunnelseSkalReturnereBegrunnelseMedTommeStrenger(){
        begrunnelseRessursTest.getBegrunnelseSkalReturnereBegrunnelseMedTommeStrenger();
    }

    @Test
    public void getBegrunnelseSkalReturnereBegrunnelse(){
        begrunnelseRessursTest.getBegrunnelseSkalReturnereBegrunnelse();
    }

    @Test
    public void putBegrunnelseSkalSetteBegrunnelse(){
        begrunnelseRessursTest.putBegrunnelseSkalSetteBegrunnelse();
    }
}
