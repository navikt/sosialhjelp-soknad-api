package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.config.SoknadActionsTestConfig;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.InnsendingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.util.Locale;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadActionsTestConfig.class})
public class SoknadActionsTest {

    public static final String BEHANDLINGS_ID = "123";
    public static final String SOKNADINNSENDING_ETTERSENDING_URL = "/soknadinnsending/ettersending";
    public static final String SAKSOVERSIKT_URL = "/saksoversikt";

    @Inject
    NavMessageSource tekster;
    @Inject
    SoknadService soknadService;
    @Inject
    OppgaveHandterer oppgaveHandterer;
    @Inject
    InnsendingService innsendingService;
    @Inject
    SystemdataUpdater systemdataUpdater;
    @Inject
    AdresseSystemdata adresseSystemdata;
    @Inject
    SoknadActions actions;
    @Inject
    private Tilgangskontroll tilgangskontroll;

    ServletContext context = mock(ServletContext.class);

    @Before
    public void setUp() {
        System.setProperty("soknadinnsending.ettersending.path", SOKNADINNSENDING_ETTERSENDING_URL);
        System.setProperty("saksoversikt.link.url", SAKSOVERSIKT_URL);
        reset(tekster);
        when(tekster.finnTekst(eq("sendtSoknad.sendEpost.epostSubject"), any(Object[].class), any(Locale.class))).thenReturn("Emne");
        when(context.getRealPath(anyString())).thenReturn("");
    }

    @Test
    public void sendSoknadSkalKalleSoknadService() {
        actions.sendSoknad(BEHANDLINGS_ID, context);

        verify(soknadService, times(1)).sendSoknad(eq(BEHANDLINGS_ID));
    }
}
