package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class BasisPersonaliaRessursUtenOidcTest {

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @Mock
    private Adressekodeverk adressekodeverk;

    @InjectMocks
    private BasisPersonaliaRessurs basisPersonaliaRessurs = spy(new BasisPersonaliaRessurs());

    @InjectMocks
    private BasisPersonaliaRessursTest basisPersonaliaRessursTest;

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
    public void getBasisPersonaliaSkalReturnereSystemBasisPersonalia(){
        basisPersonaliaRessursTest.getBasisPersonaliaSkalReturnereSystemBasisPersonalia();
    }

    @Test
    public void getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger(){
        basisPersonaliaRessursTest.getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger();
    }
}