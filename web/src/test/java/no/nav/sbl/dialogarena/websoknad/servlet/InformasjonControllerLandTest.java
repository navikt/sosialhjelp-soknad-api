package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
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

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService.EOS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonControllerLandTest {

    @Mock
    private LandService landService;

    @InjectMocks
    private InformasjonController controller;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));

        mockMvc = MockMvcBuilders.standaloneSetup(controller, new ExceptionController())
                .setMessageConverters(json)
                .setHandlerExceptionResolvers()
                .build();
    }

    @Test
    public void skalHenteLandkoder() throws Exception {
        when(landService.hentLand(null)).thenReturn(asList(new Land("Sverige", "SE"), new Land("Island", "IS"), new Land("Norge", "NOR")));

        mockMvc.perform(get("/informasjon/land"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].text").value("Sverige"))
                .andExpect(jsonPath("[0].value").value("SE"))
                .andExpect(jsonPath("[1].text").value("Island"))
                .andExpect(jsonPath("[1].value").value("IS"))
                .andExpect(jsonPath("[2].text").value("Norge"))
                .andExpect(jsonPath("[2].value").value("NOR"));
    }

    @Test
    public void skalHenteStatsborgerskapstype() throws Exception {
        Map<String, String> resultat = new HashMap<>();
        resultat.put("resultat", EOS);
        when(landService.hentStatsborgerskapstype("DNK")).thenReturn(resultat);
        mockMvc.perform(get("/informasjon/land/actions/hentstatsborgerskapstype?landkode=DNK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resultat").value(EOS));
    }
}
