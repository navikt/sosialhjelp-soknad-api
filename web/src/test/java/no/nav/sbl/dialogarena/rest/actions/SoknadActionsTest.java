package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.websoknad.domain.SoknadBekreftelse;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadActionsTest {

    public static final String BEHANDLINGS_ID = "123";

    @Mock
    NavMessageSource tekster;
    @Mock
    EmailService emailService;
    @Mock
    SoknadService soknadService;
    @Mock
    VedleggService vedleggService;
    @Mock
    HtmlGenerator pdfTemplate;

    @InjectMocks
    SoknadActions actions;

    @Before
    public void setUp() {
        when(tekster.finnTekst(eq("sendtSoknad.sendEpost.epostSubject"), any(Object[].class), any(Locale.class))).thenReturn("Emne");
    }

    @Test
    public void sendSoknadSkalLageDagpengerPdfMedKodeverksverdier() throws Exception{
        when(soknadService.hentSoknadMedFaktaOgVedlegg(BEHANDLINGS_ID)).thenReturn(soknad());
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/dagpenger"));
    }

    @Test
    public void sendGjenopptakSkalLageGjenopptakPdfMedKodeverksverdier() throws Exception{
        when(soknadService.hentSoknadMedFaktaOgVedlegg(BEHANDLINGS_ID)).thenReturn(soknad().medskjemaNummer("NAV 04-16.03"));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("/skjema/gjenopptak"));
    }

    @Test
    public void sendEttersendingSkalLageEttersendingDummyPdf() throws Exception{
        when(soknadService.hentSoknadMedFaktaOgVedlegg(BEHANDLINGS_ID)).thenReturn(soknad().medDelstegStatus(ETTERSENDING_OPPRETTET));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("<html></html>");

        actions.sendSoknad(BEHANDLINGS_ID);

        verify(pdfTemplate).fyllHtmlMalMedInnhold(any(WebSoknad.class), eq("skjema/ettersending/dummy"));
    }

    @Test
    public void soknadBekreftelseEpostSkalInneholdeSoknadbekreftelseTekst() {
        SoknadBekreftelse soknadBekreftelse = new SoknadBekreftelse();
        soknadBekreftelse.setEpost("test@nav.no");
        soknadBekreftelse.setErEttersendelse(false);

        actions.sendEpost(BEHANDLINGS_ID, soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendtSoknad.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    @Test
    public void ettersendingBekreftelseEpostSkalInneholdeEttersendingbekreftelseTekst() {
        SoknadBekreftelse soknadBekreftelse = new SoknadBekreftelse();
        soknadBekreftelse.setEpost("test@nav.no");
        soknadBekreftelse.setErEttersendelse(true);

        actions.sendEpost("123", soknadBekreftelse, new MockHttpServletRequest());

        verify(tekster).finnTekst(eq("sendEttersendelse.sendEpost.epostInnhold"), any(Object[].class), any(Locale.class));
    }

    private WebSoknad soknad() {
        return new WebSoknad().medBehandlingId(BEHANDLINGS_ID);
    }
}
