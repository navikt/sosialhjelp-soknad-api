package no.nav.sbl.dialogarena.websoknad.servlet;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.Arrays;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class VedleggControllerTest {

    @Mock
    private VedleggService vedleggService;

    @InjectMocks
    private VedleggController controller;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(json)
                .build();
    }

    private static final byte[] PDF = new byte[]{0x25, 0x50, 0x44, 0x46, 0x01, 0x01};

    @Test
    public void shouldUploadFile() throws Exception {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(vedleggService.hentVedlegg(11L, 3L, false)).thenReturn(new Vedlegg().medVedleggId(3L));
        when(vedleggService.splitOgLagreVedlegg(any(Vedlegg.class), any(InputStream.class))).thenReturn(asList(11L, 12L));
        when(vedleggService.hentVedlegg(eq(11L), eq(11L), eq(false))).thenReturn(new Vedlegg().medVedleggId(11L));
        when(vedleggService.hentVedlegg(eq(11L), eq(12L), eq(false))).thenReturn(new Vedlegg().medVedleggId(12L));
        mockMvc.perform(fileUpload("/soknad/11/vedlegg/3/opplasting")
                .file(createFile("test.pdf", PDF))
                .param("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(11L))
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("files[0].vedleggId").value(11))
                .andExpect(jsonPath("files[1].vedleggId").value(12));
    }

    private MockMultipartFile createFile(String fileName, byte[] data) {
        return new MockMultipartFile("files[]", fileName, MediaType.MULTIPART_FORM_DATA_VALUE, data);
    }

    @Test
    public void shouldGetPreview() throws Exception {
        byte[] preview = {1, 2, 3};
        when(vedleggService.lagForhandsvisning(11L, 12L, 0)).thenReturn(preview);
        mockMvc.perform(get("/soknad/{soknadId}/vedlegg/{vedleggId}/thumbnail", 11, 12)
                .accept(MediaType.ALL));
        verify(vedleggService).lagForhandsvisning(11L, 12L, 0);
    }

    @Test
    public void skalHenteUnderBehandling() throws Exception {

        when(vedleggService.hentVedlegg(11L, 12L, false)).thenReturn(new Vedlegg().medVedleggId(11L).medFaktumId(123L).medFillagerReferanse("fillagerref"));
        when(vedleggService.hentVedleggUnderBehandling(11L, "fillagerref")).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(1234L)));
        mockMvc.perform(get("/soknad/{soknadId}/vedlegg/{vedleggId}/underBehandling", 11, 12)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("[0].vedleggId").value(1234));
        verify(vedleggService).hentVedleggUnderBehandling(11L, "fillagerref");
    }


    @Test
    public void skalSletteVedlegg() throws Exception {
        doNothing().when(vedleggService).slettVedlegg(11L, 12L);
        mockMvc.perform(post("/soknad/{soknadId}/vedlegg/{vedlegg}/delete", 11L, 12L))
                .andExpect(status().isOk());
        verify(vedleggService).slettVedlegg(11L, 12L);
    }


    @Test
    public void skalLagreVedlegg() throws Exception {
        Vedlegg v = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(11L)
                .medFaktumId(1L);
        when(vedleggService.hentVedlegg(11L, 1L, false)).thenReturn(v);
        mockMvc.perform(post("/soknad/{soknadId}/vedlegg/{vedleggId}", 11L, 1L)
                .content("{\"vedleggId\": 1, \"soknadId\": 11, \"faktumId\": 1, \"fillagerReferanse\": \"" + v.getFillagerReferanse() + "\"} ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(vedleggService).lagreVedlegg(eq(11L), eq(1L), eq(v));
    }
}
