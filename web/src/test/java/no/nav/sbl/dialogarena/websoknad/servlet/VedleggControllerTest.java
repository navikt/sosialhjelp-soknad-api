package no.nav.sbl.dialogarena.websoknad.servlet;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@Ignore //Testene feiler innimellom
public class VedleggControllerTest {

    @Mock
    private VedleggService vedleggService;

    @InjectMocks
    private VedleggController controller;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(json)
                .build();
    }

    private static final byte[] PDF = new byte[]{0x25, 0x50, 0x44, 0x46, 0x01, 0x01};
    private static final byte[] PNG = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};

    @Test
    public void shouldUploadFile() throws Exception {
        when(vedleggService.splitOgLagreVedlegg(any(Vedlegg.class), any(InputStream.class))).thenReturn(asList(1L)).thenReturn(asList(2L)).thenReturn(asList(3L));
        when(vedleggService.hentVedlegg(eq(11L), eq(1L), eq(false))).thenReturn(new Vedlegg(1L, 11L, 12L, "L6", "test", 1L, 1, "gfdg", null, Vedlegg.Status.VedleggKreves));
        when(vedleggService.hentVedlegg(eq(11L), eq(2L), eq(false))).thenReturn(new Vedlegg(2L, 11L, 12L, "L6", "test", 1L, 1, "gfdg", null, Vedlegg.Status.VedleggKreves));
        when(vedleggService.hentVedlegg(eq(11L), eq(3L), eq(false))).thenReturn(new Vedlegg(3L, 11L, 12L, "L6", "test", 1L, 1, "gfdg", null, Vedlegg.Status.VedleggKreves));
        MvcResult mvcResult = mockMvc.perform(fileUpload("/soknad/11/faktum/12/vedlegg?gosysId=L6")
                .file(createFile("test.pdf", PDF))
                .file(createFile("test.jpg", JPEG))
                .file(createFile("test.png", PNG))
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(request().asyncStarted())
                .andReturn();
        Thread.sleep(500); //Spring async har en bug som gjør at det noen ganger feiler. Denne sleepen fikser det.
        mockMvc.perform(asyncDispatch(mvcResult))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("files[0].vedlegg.id").value(1))
                .andExpect(jsonPath("files[1].vedlegg.id").value(2))
                .andExpect(jsonPath("files[2].vedlegg.id").value(3));
    }

    private MockMultipartFile createFile(String fileName, byte[] data) {
        return new MockMultipartFile("files[]", fileName, MediaType.MULTIPART_FORM_DATA_VALUE, data);
    }

    @Test
    public void skalAvviseMedUnnsupportetMediaVedFeilType() throws Exception {
        MvcResult mvcResult = mockMvc.perform(fileUpload("/soknad/11/faktum/19/vedlegg")
                .file(createFile("test.doc", new byte[]{1, 2, 3}))
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(instanceOf(OpplastingException.class)))
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("kode").value("vedlegg.opplasting.feil.filtype"));
    }

    @Test
    public void shouldGetPreview() throws Exception {
        byte[] preview = {1, 2, 3};
        when(vedleggService.lagForhandsvisning(11L, 12L, 0)).thenReturn(preview);
        mockMvc.perform(get("/soknad/{soknadId}/faktum/{faktumId}/vedlegg/{vedleggId}/thumbnail", 11, 13, 12)
                .accept(MediaType.ALL))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(preview));
        verify(vedleggService).lagForhandsvisning(11L, 12L, 0);
    }


    @Test
    public void skalSletteVedlegg() throws Exception {
        doNothing().when(vedleggService).slettVedlegg(11L, 12L);
        mockMvc.perform(post("/soknad/{soknadId}/faktum/{faktumId}/vedlegg/{vedlegg}/delete", 11L, 13L, 12L))
                .andExpect(status().isOk());
        verify(vedleggService).slettVedlegg(11L, 12L);
    }

    @Test
    public void skalGenereFerdigPdf() throws Exception {
        Vedlegg v = new Vedlegg(1L, 11L, 1L, "1", "", 3L, 1, null, null, Vedlegg.Status.VedleggKreves);
        when(vedleggService.genererVedleggFaktum(11L, 14L)).thenReturn(2L);
        when(vedleggService.hentVedlegg(11L, 2L, false)).thenReturn(v);
        MvcResult mvcResult = mockMvc.perform(post("/soknad/{soknadId}/faktum/{faktumId}/vedlegg/generer", 11L, 14L))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(instanceOf(Vedlegg.class)))
                .andReturn();
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(v.getVedleggId().intValue()));
    }
}
