package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
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
public class SoknadBekreftelseControllerTest {
    @Mock
    private EmailService emailService;

    @Mock
    private ConfigService configService;

    @Spy
    private StaticMessageSource messageSource = new StaticMessageSource();

    @InjectMocks
    private SoknadBekreftelseController controller;
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
    public void skalKunneGenerereEttersendelseUrl() {
        Assert.assertEquals("http://a34duvw22583.devillo.no:8181/sendsoknad/startettersending/123",
                ServerUtils.getEttersendelseUrl("http://a34duvw22583.devillo.no:8181/sendsoknad/rest/soknad/skjemanavn#/bekreftelse/123", "123"));
    }

    @Test
    public void skalSendeEpost() throws Exception {
        messageSource.addMessage("sendtSoknad.sendEpost.epostInnhold", new Locale("nb", "NO"), "Tekst. Saksoversikturl {0} og ettersendelseurl {1}");
        messageSource.addMessage("sendtSoknad.sendEpost.epostSubject", new Locale("nb", "NO"), "emne");

        mockMvc.perform(post("/bekreftelse/{behandlingId}", "123").contentType(MediaType.APPLICATION_JSON).content("{\"epost\": \"test@epost.com\", \"temaKode\": \"DAG\", \"erEttersendelse\": \"false\"}"))
                .andExpect(status().isOk());
        verify(emailService).sendEpostEtterInnsendtSoknad("test@epost.com", "emne", "Tekst. Saksoversikturl null/detaljer/DAG/123 og ettersendelseurl http://localhost:80/bekreftelse/123/startettersending/123", "123");
    }

    @Test
    public void skalSendeEpostMedEttersendelseInnhold() throws Exception {
        messageSource.addMessage("sendtSoknad.sendEpost.epostInnhold", new Locale("nb", "NO"), "Tekst. Saksoversikturl {0} og ettersendelseurl {1}");
        messageSource.addMessage("sendEttersendelse.sendEpost.epostInnhold", new Locale("nb", "NO"), "Ettersendelse. Saksoversikturl {0}");
        messageSource.addMessage("sendtSoknad.sendEpost.epostSubject", new Locale("nb", "NO"), "emne");

        mockMvc.perform(post("/bekreftelse/{behandlingId}", "123").contentType(MediaType.APPLICATION_JSON).content("{\"epost\": \"test@epost.com\", \"temaKode\": \"DAG\", \"erEttersendelse\": true}"))
                .andExpect(status().isOk());
        verify(emailService).sendEpostEtterInnsendtSoknad("test@epost.com", "emne", "Ettersendelse. Saksoversikturl null/detaljer/DAG/123", "123");
    }
}
