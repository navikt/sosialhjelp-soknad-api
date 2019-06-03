package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TelefonnummerRessursUtenOidcTest {

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private TelefonnummerSystemdata telefonnummerSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private TelefonnummerRessurs telefonnummerRessurs = spy(new TelefonnummerRessurs());

    @InjectMocks
    private TelefonnummerRessursTest telefonnummerRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        doCallRealMethod().when(telefonnummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class));
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getTelefonnummerSkalReturnereSystemTelefonnummer(){
        telefonnummerRessursTest.getTelefonnummerSkalReturnereSystemTelefonnummer();
    }

    @Test
    public void getTelefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull(){
        telefonnummerRessursTest.getTelefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull();
    }
    @Test
    public void getTelefonnummerSkalReturnereBrukerutfyltTelefonnummer(){
        telefonnummerRessursTest.getTelefonnummerSkalReturnereBrukerutfyltTelefonnummer();
    }

    @Test
    public void putTelefonnummerSkalLageNyJsonTelefonnummerDersomDenVarNull(){
        telefonnummerRessursTest.putTelefonnummerSkalLageNyJsonTelefonnummerDersomDenVarNull();
    }

    @Test
    public void putTelefonnummerSkalOppdatereBrukerutfyltTelefonnummer(){
        telefonnummerRessursTest.putTelefonnummerSkalOppdatereBrukerutfyltTelefonnummer();
    }

    @Test
    public void putTelefonnummerSkalOverskriveBrukerutfyltTelefonnummerMedSystemTelefonnummer(){
        telefonnummerRessursTest.putTelefonnummerSkalOverskriveBrukerutfyltTelefonnummerMedSystemTelefonnummer();
    }
}
