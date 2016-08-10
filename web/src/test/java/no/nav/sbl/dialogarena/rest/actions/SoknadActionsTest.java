package no.nav.sbl.dialogarena.rest.actions;

import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.config.SoknadActionsTestConfig;
import no.nav.sbl.dialogarena.rest.meldinger.SoknadBekreftelse;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    EmailService emailService;
    @Inject
    SoknadService soknadService;
    @Inject
    VedleggService vedleggService;
    @Inject
    HtmlGenerator pdfTemplate;
    @Inject
    SoknadActions actions;
    @Inject
    WebSoknadConfig webSoknadConfig;


    ServletContext context = mock(ServletContext.class);

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        System.setProperty("soknadinnsending.ettersending.path", SOKNADINNSENDING_ETTERSENDING_URL);
        System.setProperty("saksoversikt.link.url", SAKSOVERSIKT_URL);
        reset(tekster);
        when(tekster.finnTekst(eq("sendtSoknad.sendEpost.epostSubject"), any(Object[].class), any(Locale.class))).thenReturn("Emne");
        when(context.getRealPath(anyString())).thenReturn("");
        when(webSoknadConfig.brukerNyOppsummering(anyLong())).thenReturn(false);
        when(webSoknadConfig.skalSendeMedFullSoknad(anyLong())).thenReturn(false);
        actions.setContext(context);
    }

    @Test
    public void sendSoknadSkalLageDagpengerPdfMedKodeverksverdier() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medSoknadPrefix("dagpenger.ordinaer"));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/dagpenger.ordinaer"));
    }

    @Test
    public void sendSoknadSkalBrukeNyPdfLogikkOmDetErSattPaaConfig() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medSoknadPrefix("dagpenger.ordinaer"));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyBoolean())).thenReturn("<html></html>");
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");
        when(webSoknadConfig.brukerNyOppsummering(anyLong())).thenReturn(true);

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), anyBoolean());
        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/kvittering"));
    }

    @Test
    public void sendGjenopptakSkalLageGjenopptakPdfMedKodeverksverdier() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medSoknadPrefix("dagpenger.gjenopptak"));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/dagpenger.gjenopptak"));
    }

    @Test
    public void sendEttersendingSkalLageEttersendingDummyPdf() throws Exception {
        when(soknadService.hentSoknad(BEHANDLINGS_ID, true, true)).thenReturn(soknad().medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("skjema/ettersending/dummy"));
    }

    @Test
    public void soknadBekreftelseEpostSkalInneholdeSoknadbekreftelseTekst() {
        Faktum sprakFaktum = new Faktum().medKey("skjema.sprak").medValue("nb_NO");
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad().medFaktum(sprakFaktum));
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());
        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    @Test
    public void soknadBekreftelseEpostSkalBrukeNorskSomDefaultLocale() {
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), any(Object[].class), eq(new Locale("nb", "NO")));
    }


    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostForTypeSoknadsdialoger() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, true);
        soknadBekreftelse.setTemaKode("DAG");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/DAG", "/soknadinnsending/ettersending/123");

    }

    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostForTypeDokumentinnsending() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(false, false);
        soknadBekreftelse.setTemaKode("KON");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/KON", "/saksoversikt/app/ettersending");

    }

    @Test
    public void soknadBekreftelseEpostSkalSendeRettParametreTilEpostSoknadsdialogerOgEttersendelse() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(true, true);
        soknadBekreftelse.setTemaKode("DAG");

        actions.sendEpost(BEHANDLINGS_ID, "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendEttersendelse.sendEpost.epostInnhold"), captor.capture(), eq(new Locale("nb", "NO")));
        assertThat(captor.getValue()).containsSequence("/saksoversikt/app/tema/DAG");

    }

    @Test
    public void ettersendingBekreftelseEpostSkalInneholdeEttersendingbekreftelseTekst() {
        SoknadBekreftelse soknadBekreftelse = lagSoknadBekreftelse(true, true);

        actions.sendEpost("123", "nb_NO", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendEttersendelse.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    private SoknadBekreftelse lagSoknadBekreftelse(boolean erEttersendelse, boolean erSoknadsdialog) {
        SoknadBekreftelse soknadBekreftelse = new SoknadBekreftelse();
        soknadBekreftelse.setEpost("test@nav.no");
        soknadBekreftelse.setErEttersendelse(erEttersendelse);
        soknadBekreftelse.setErSoknadsdialog(erSoknadsdialog);
        return soknadBekreftelse;
    }

    private WebSoknad soknad() {
        return new WebSoknad().medBehandlingId(BEHANDLINGS_ID);
    }

}
