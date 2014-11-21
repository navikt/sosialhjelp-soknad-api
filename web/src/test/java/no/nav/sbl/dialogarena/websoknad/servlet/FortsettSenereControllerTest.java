package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Locale;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class FortsettSenereControllerTest {
    @Mock
    private EmailService emailService;
    @Spy
    private StaticMessageSource messageSource = new StaticMessageSource();

    @InjectMocks
    private FortsettSenereController controller;
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
    public void skalKunneGenerereGjenopptaUrl() {
        Assert.assertEquals("http://a34duvw22583.devillo.no:8181/sendsoknad/soknad/skjemanavn#/behandling/fortsett?utm_source=web&utm_medium=email&utm_campaign=2",
                ServerUtils.getGjenopptaUrl("http://a34duvw22583.devillo.no:8181/sendsoknad/rest/soknad/244/fortsettsenere", "skjemanavn", "behandling"));
    }

    @Test
    public void skalGjennoppta() throws Exception {
        messageSource.addMessage("fortsettSenere.sendEpost.epostInnhold", new Locale("nb", "NO"), "test med url {0}");
        mockMvc.perform(post("/soknad/{soknadId}/{behandlingsId}/fortsettsenere", "NAV13", "BH123").contentType(MediaType.APPLICATION_JSON).content("{\"epost\": \"test@epost.com\"}"))
                .andExpect(status().isOk());
        verify(emailService).sendFortsettSenereEPost("test@epost.com", "Lenke til påbegynt dagpengesøknad", "test med url http://localhost:80/soknad/NAV13/BH123/fortsettsenere/soknad/NAV13#/BH123/fortsett?utm_source=web&utm_medium=email&utm_campaign=2");
    }

}
