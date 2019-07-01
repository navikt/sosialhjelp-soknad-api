package no.nav.sbl.dialogarena.rest.ressurser.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.ArbeidsforholdSystemdata;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidRessursUtenOidcTest {

    @Mock
    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private ArbeidRessurs arbeidRessurs = spy(new ArbeidRessurs());

    @InjectMocks
    private ArbeidRessursTest arbeidRessursTest;

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
    public void getArbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert(){
        arbeidRessursTest.getArbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert();
    }

    @Test
    public void getArbeidSkalReturnereArbeidsforholdLikNull(){
        arbeidRessursTest.getArbeidSkalReturnereArbeidsforholdLikNull();
    }

    @Test
    public void getArbeidSkalReturnereKommentarTilArbeidsforholdLikNull(){
        arbeidRessursTest.getArbeidSkalReturnereKommentarTilArbeidsforholdLikNull();
    }

    @Test
    public void getArbeidSkalReturnereKommentarTilArbeidsforhold(){
        arbeidRessursTest.getArbeidSkalReturnereKommentarTilArbeidsforhold();
    }

    @Test
    public void putArbeidSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull(){
        arbeidRessursTest.putArbeidSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull();
    }

    @Test
    public void putArbeidSkalOppdatereKommentarTilArbeidsforhold(){
        arbeidRessursTest.putArbeidSkalOppdatereKommentarTilArbeidsforhold();
    }
}
