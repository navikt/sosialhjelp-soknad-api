package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.print.HtmlToPdf;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class SoknadDataControllerTest {
    @Mock
    private VedleggService vedleggService;
    @Mock
    private SendSoknadService soknadService;

    @Mock
    private HtmlGenerator pdfTemplate;

    @Mock
    private HtmlToPdf pdfgenerator;

    @InjectMocks
    private SoknadDataController controller;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));

        mockMvc = MockMvcBuilders.standaloneSetup(controller, new ExceptionController())
                .setMessageConverters(json)
                .setHandlerExceptionResolvers()
                .build();
    }

    @Test
    public void skalHenteDataFraDiverseEndpoints() throws Exception {
        when(soknadService.hentSoknad(11L)).thenReturn(WebSoknad.startSoknad().medId(11L).medOppretteDato(new DateTime()).medskjemaNummer("NAV 04-01.03"));
        mockMvc.perform(get("/soknad/{soknadId}", 11L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("soknadId").value(11));
        when(soknadService.hentSoknad(11L)).thenReturn(WebSoknad.startSoknad().medId(11L).medOppretteDato(new DateTime()));
        mockMvc.perform(get("/soknad/metadata/{soknadId}", 11L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("soknadId").value(11));
        when(soknadService.hentSoknadMedBehandlingsId("123")).thenReturn(new WebSoknad().medId(15L).medStatus(UNDER_ARBEID));
        mockMvc.perform(get("/soknad/behandling/{behandlingsId}", "123").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("15"));
        when(vedleggService.hentPaakrevdeVedlegg(123L)).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(16L).medFaktumId(12L)));
        mockMvc.perform(get("/soknad/{soknadId}/{faktumId}/forventning", "123", 12).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].vedleggId").value(16));
    }

    @Test
    public void skalOppdatereDelstegStatus() throws Exception {

        mockMvc.perform(post("/soknad/delsteg/{soknadId}/{delsteg}", 11L, "utfylling").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(soknadService).settDelsteg(11L, DelstegStatus.UTFYLLING);
        mockMvc.perform(post("/soknad/delsteg/{soknadId}/{delsteg}", 11L, "vedlegg").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(soknadService).settDelsteg(11L, DelstegStatus.SKJEMA_VALIDERT);
        mockMvc.perform(post("/soknad/delsteg/{soknadId}/{delsteg}", 11L, "oppsummering").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(soknadService).settDelsteg(11L, DelstegStatus.VEDLEGG_VALIDERT);
    }

    @Test
    public void skalOppretteSoknad() throws Exception {
        when(soknadService.startSoknad(anyString())).thenReturn("123");
        mockMvc.perform(post("/soknad/opprett", 11L).contentType(MediaType.APPLICATION_JSON)
                .content("{\"soknadType\":\"NAV 03-01.04\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("brukerbehandlingId").value("123"));
        verify(soknadService).startSoknad("NAV 03-01.04");
    }

    @Test
    public void skalSendeSoknadOgLeggeVedKvittering() throws Exception {
        byte[] outputBytes = new byte[]{1, 2, 3};
        List<Vedlegg> vedlegg = Arrays.asList(new Vedlegg().medVedleggId(123L));
        when(soknadService.hentSoknad(11L)).thenReturn(WebSoknad.startSoknad().medVedlegg(vedlegg));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn("html");
        when(pdfgenerator.lagPdfFil(anyString())).thenReturn(outputBytes);
        mockMvc.perform(post("/soknad/send/{soknadId}", 11L).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(vedleggService, times(2)).leggTilKodeverkFelter(vedlegg);
        verify(vedleggService).lagreKvitteringSomVedlegg(11L, outputBytes);
        verify(pdfgenerator, times(2)).lagPdfFil("html");
        verify(soknadService).sendSoknad(11L, outputBytes);
    }

    @Test
    public void skalAvbryteSoknad() throws Exception {
        when(soknadService.startSoknad(anyString())).thenReturn("123");
        mockMvc.perform(post("/soknad/delete/{soknadId}", 11L))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
        verify(soknadService).avbrytSoknad(11L);
    }

    @Test
    public void skalHenteOppsumering() throws Exception {
        String html = "<html>testinnhold</html>";
        List<Vedlegg> vedlegg = Arrays.asList(new Vedlegg().medVedleggId(123L));
        when(soknadService.hentSoknad(11L)).thenReturn(WebSoknad.startSoknad().medVedlegg(vedlegg));
        when(pdfTemplate.fyllHtmlMalMedInnhold(any(WebSoknad.class), anyString())).thenReturn(html);
        mockMvc.perform(get("/soknad/oppsummering/{soknadId}", 11L))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + html + "\""));
        verify(vedleggService).leggTilKodeverkFelter(vedlegg);
    }

}
