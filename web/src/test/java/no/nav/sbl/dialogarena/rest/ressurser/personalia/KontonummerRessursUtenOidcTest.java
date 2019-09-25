package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.KontonummerSystemdata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
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
public class KontonummerRessursUtenOidcTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String KONTONUMMER_BRUKER = "11122233344";
    private static final String KONTONUMMER_SYSTEM = "44333222111";
    private static final String KONTONUMMER_SYSTEM_OPPDATERT = "44333222123";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private KontonummerSystemdata kontonummerSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private KontonummerRessurs kontonummerRessurs = spy(new KontonummerRessurs());

    @InjectMocks
    private KontonummerRessursTest kontonummerRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        doCallRealMethod().when(kontonummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class), any());
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getKontonummerSkalReturnereSystemKontonummer(){
        kontonummerRessursTest.getKontonummerSkalReturnereSystemKontonummer();
    }

    @Test
    public void getKontonummerSkalReturnereBrukerutfyltKontonummer(){
        kontonummerRessursTest.getKontonummerSkalReturnereBrukerutfyltKontonummer();
    }

    @Test
    public void getKontonummerSkalReturnereKontonummerLikNull(){
        kontonummerRessursTest.getKontonummerSkalReturnereKontonummerLikNull();
    }
    @Test
    public void putKontonummerSkalSetteBrukerutfyltKontonummer(){
        kontonummerRessursTest.putKontonummerSkalSetteBrukerutfyltKontonummer();
    }

    @Test
    public void putKontonummerSkalOverskriveBrukerutfyltKontonummerMedSystemKontonummer(){
        kontonummerRessursTest.putKontonummerSkalOverskriveBrukerutfyltKontonummerMedSystemKontonummer();
    }

}
