package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService.EOS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class LandControllerTest {
    @Mock
    private Kodeverk kodeverk;

    @Mock
    private LandService landService;

    @InjectMocks
    private LandController controller;
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
    public void skalHenteLandkoder() throws Exception {
        when(kodeverk.getAlleLandkoder()).thenReturn(Arrays.asList("SE", "IS", "PL", "NOR"));
        when(kodeverk.getLand("SE")).thenReturn("Sverige");
        when(kodeverk.getLand("IS")).thenReturn("Island");
        when(kodeverk.getLand("PL")).thenReturn("Polen");
        when(kodeverk.getLand("NOR")).thenReturn("Norge");

        mockMvc.perform(get("/land"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result[0].text").value("Sverige"))
                .andExpect(jsonPath("result[0].value").value("SE"))
                .andExpect(jsonPath("result[1].text").value("Island"))
                .andExpect(jsonPath("result[1].value").value("IS"))
                .andExpect(jsonPath("result[2].text").value("Polen"))
                .andExpect(jsonPath("result[2].value").value("PL"))
                .andExpect(jsonPath("result[3].text").value("Norge"))
                .andExpect(jsonPath("result[3].value").value("NOR"));
        verify(kodeverk).getAlleLandkoder();
    }

    @Test
    public void skalLeggeTilNorge() throws Exception {
        when(kodeverk.getAlleLandkoder()).thenReturn(Arrays.asList("SE"));
        when(kodeverk.getLand("SE")).thenReturn("Sverige");
        mockMvc.perform(get("/land"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result[0].text").value("Norge"))
                .andExpect(jsonPath("result[0].value").value("NOR"))
                .andExpect(jsonPath("result[1].text").value("Sverige"))
                .andExpect(jsonPath("result[1].value").value("SE"));
        verify(kodeverk).getAlleLandkoder();
    }

    @Test
    public void skalHenteStatsborgerskapstype() throws Exception {
        when(landService.getStatsborgeskapType(any(String.class))).thenReturn(EOS);
        mockMvc.perform(get("/land/statsborgerskap/type/DNK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value(EOS));
    }
}
