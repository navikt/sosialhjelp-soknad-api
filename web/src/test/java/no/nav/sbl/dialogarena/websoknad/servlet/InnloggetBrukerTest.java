package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class InnloggetBrukerTest {
    @Mock
    private Kodeverk kodeverk;
    @Mock
    private InnloggetBruker innloggetBruker;

    @InjectMocks
    private InformasjonController controller;
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
    public void skalHenteKodeverk() throws Exception {
        when(kodeverk.getPoststed(any(String.class))).thenReturn("poststed");
        mockMvc.perform(get("/informasjon/poststed?postnummer=1234"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"poststed\""));
        verify(kodeverk).getPoststed("1234");
    }

    //TODO fiks test
    @Test
    @Ignore
    public void skalLagrePersonalia() throws Exception {
        mockMvc.perform(post("/soknad/personalia/11").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(innloggetBruker).lagrePersonaliaOgBarn(11L, false);
    }

    //TODO fiks test
    @Test
    @Ignore
    public void skalLagrePersonaliaOgBarn() throws Exception {
        mockMvc.perform(post("/soknad/personalia/11").contentType(MediaType.APPLICATION_JSON).content("true"))
                .andExpect(status().isOk());
        verify(innloggetBruker).lagrePersonaliaOgBarn(11L, true);
    }

    @Test
    public void skalHentePersonalia() throws Exception {
        Personalia personalia = new Personalia();
        personalia.setFnr(SubjectHandler.getSubjectHandler().getUid());
        personalia.setNavn("testnavn");
        when(innloggetBruker.hentPersonalia()).thenReturn(personalia);
        mockMvc.perform(get("/informasjon/personalia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fnr").value(SubjectHandler.getSubjectHandler().getUid()))
                .andExpect(jsonPath("navn").value("testnavn"));
    }
}
