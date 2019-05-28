package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseRessursUtenOidcTest {

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private AdresseSystemdata adresseSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private NavEnhetRessurs navEnhetRessurs;

    @InjectMocks
    private AdresseRessurs adresseRessurs = spy(new AdresseRessurs());

    @InjectMocks
    private AdresseRessursTest adresseRessursTest;


    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getAdresserSkalReturnereAdresserRiktigKonvertert(){
        adresseRessursTest.getAdresserSkalReturnereAdresserRiktigKonvertert();
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse(){
        adresseRessursTest.getAdresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse();
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse(){
        adresseRessursTest.getAdresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse();
    }

    @Test
    public void getAdresserSkalReturnereAdresserLikNull(){
        adresseRessursTest.getAdresserSkalReturnereAdresserLikNull();
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet(){
        adresseRessursTest.putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet();
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet(){
        adresseRessursTest.putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet();
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikSoknadsadresseOgReturnereTilhorendeNavenhet(){
        adresseRessursTest.putAdresseSkalSetteOppholdsAdresseLikSoknadsadresseOgReturnereTilhorendeNavenhet();
    }
}