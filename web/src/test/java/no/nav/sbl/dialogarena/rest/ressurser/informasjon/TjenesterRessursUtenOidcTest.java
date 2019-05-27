package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursUtenOidcTest {

    private String fodselsnummer;

    @Mock
    private AktivitetService aktivitetService;

    @Mock
    private MaalgrupperService maalgrupperService;

    @InjectMocks
    private TjenesterRessurs ressurs = spy(new TjenesterRessurs());

    @InjectMocks
    private TjenesterRessursTest tjenesterRessursTest;

    @Before
    public void setUp() throws Exception {
        setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        tjenesterRessursTest.fodselsnummer = SamlStaticSubjectHandler.getSubjectHandler().getUid();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void skalHenteAktiviteter() throws Exception {
        tjenesterRessursTest.skalHenteAktiviteter();
    }

    @Test
    public void skalHenteMaalgrupper() throws Exception {
        tjenesterRessursTest.skalHenteMaalgrupper();
    }
}