package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BasisPersonaliaSystemdata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasisPersonaliaRessursUtenOidcTest {

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private BasisPersonaliaSystemdata basisPersonaliaSystemdata;

    @Mock
    private Adressekodeverk adressekodeverk;

    @Mock
    private VedleggOriginalFilerService vedleggOriginalFilerService;

    @InjectMocks
    private BasisPersonaliaRessurs basisPersonaliaRessurs = spy(new BasisPersonaliaRessurs());

    @InjectMocks
    private BasisPersonaliaRessursTest basisPersonaliaRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(anyString())).thenReturn(null);
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
    public void getBasisPersonaliaSkalReturnereOppdatertSystemBasisPersonaliaFraTPS(){
        basisPersonaliaRessursTest.getBasisPersonaliaSkalReturnereOppdatertSystemBasisPersonaliaFraTPS();
    }

    @Test
    public void getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger(){
        basisPersonaliaRessursTest.getBasisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger();
    }
}