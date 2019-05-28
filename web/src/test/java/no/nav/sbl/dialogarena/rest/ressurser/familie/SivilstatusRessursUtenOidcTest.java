package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;

import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class SivilstatusRessursUtenOidcTest {

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private SivilstatusRessurs sivilstatusRessurs = spy(new SivilstatusRessurs());

    @InjectMocks
    private SivilstatusRessursTest sivilstatusRessursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void getSivilstatusSkalReturnereNull(){
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void getSivilstatusSkalReturnereKunBrukerdefinertStatus(){
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void getSivilstatusSkalReturnereBrukerdefinertEktefelleRiktigKonvertert(){
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void getSivilstatusSkalReturnereSystemdefinertEktefelleRiktigKonvertert(){
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void getSivilstatusSkalReturnereSystemdefinertEktefelleMedDiskresjonskode(){
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void putSivilstatusSkalKunneSetteAlleTyperSivilstatus() throws ParseException {
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }

    @Test
    public void putSivilstatusSkalSetteStatusGiftOgEktefelle() throws ParseException {
        sivilstatusRessursTest.getSivilstatusSkalReturnereNull();
    }
}
