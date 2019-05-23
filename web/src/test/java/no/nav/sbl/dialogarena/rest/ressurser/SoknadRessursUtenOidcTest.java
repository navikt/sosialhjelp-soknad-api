package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;

import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadRessursUtenOidcTest {

    public static final String BEHANDLINGSID = "123";

    @Mock
    SoknadService soknadService;

    @Mock
    XsrfGenerator xsrfGenerator;

    @Mock
    SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    SoknadRessurs ressurs = spy(new SoknadRessurs());

    @InjectMocks
    SoknadRessursTest ressursTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        ressursTest.type = new StartSoknad();
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void hentingAvSoknadSkalSetteXsrfToken() {
        ressursTest.hentingAvSoknadSkalSetteXsrfToken();
    }

    @Test
    public void opprettingAvSoknadSkalSetteXsrfToken() {
        ressursTest.opprettingAvSoknadSkalSetteXsrfToken();
    }

    @Test
    public void opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        ressursTest.opprettSoknadUtenBehandlingsidSkalStarteNySoknad();
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        ressursTest.opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending();
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        ressursTest.opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending();
    }

    @Test(expected = BadRequestException.class)
    public void oppdaterSoknadUtenParametreSkalKasteException() {
        ressursTest.oppdaterSoknadUtenParametreSkalKasteException();
    }

    @Test
    public void oppdaterSoknadMedDelstegUtfyllingSkalSetteRiktigDelstegStatus() {
        ressursTest.oppdaterSoknadMedDelstegUtfyllingSkalSetteRiktigDelstegStatus();
    }

    @Test
    public void oppdaterSoknadMedDelstegOpprettetSkalSetteRiktigDelstegStatus() {
        ressursTest.oppdaterSoknadMedDelstegOpprettetSkalSetteRiktigDelstegStatus();
    }

    @Test
    public void oppdaterSoknadMedDelstegVedleggSkalSetteRiktigDelstegStatus() {
        ressursTest.oppdaterSoknadMedDelstegVedleggSkalSetteRiktigDelstegStatus();
    }

    @Test
    public void oppdaterSoknadMedDelstegOppsummeringSkalSetteRiktigDelstegStatus() {
        ressursTest.oppdaterSoknadMedDelstegOppsummeringSkalSetteRiktigDelstegStatus();
    }

    @Test
    public void oppdaterSoknadMedJournalforendeenhetSkalSetteJournalforendeEnhet() {
        ressursTest.oppdaterSoknadMedJournalforendeenhetSkalSetteJournalforendeEnhet();
    }
}