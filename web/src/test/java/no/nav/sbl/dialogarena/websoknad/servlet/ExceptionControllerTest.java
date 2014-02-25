package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ExceptionControllerTest.Oppsett.class, ExceptionController.class, ExceptionControllerTest.Feilmeldingsgenerator.class})
public class ExceptionControllerTest {


    @Autowired
    private WebApplicationContext appContext;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        mockMvc = MockMvcBuilders.webAppContextSetup(appContext).build();
    }

    @Test
    public void skalHandtereUgyldingOpplasting() throws Exception {
        mockMvc.perform(get("/ugyldigOpplasting").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("kode").value("feil1"))
        ;
    }

    @Test
    public void skalHandtereUgyldingOpplasting2() throws Exception {
        mockMvc.perform(get("/ugyldigOpplasting2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("kode").value("feil2"))
        ;
    }

    @Test
    public void skalHandtereApplicationException() throws Exception {
        mockMvc.perform(get("/applicationException").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("kode").value("appex"))
        ;
    }

    @Test
    public void skalAlleThrowables() throws Exception {
        mockMvc.perform(get("/nullpointer").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("kode").value("generell"))
        ;
    }


    @Configuration
    @EnableWebMvc
    public static class Oppsett {
    }

    @Controller
    public static class Feilmeldingsgenerator {
        @RequestMapping("/ugyldigOpplasting")
        public void kasterUgyldigOpplastingTypeException() {
            throw new UgyldigOpplastingTypeException("test", null, "feil1");
        }

        @RequestMapping("/ugyldigOpplasting2")
        public void kasterUgyldigOpplasting2TypeException() {
            throw new OpplastingException("test", null, "feil2");
        }

        @RequestMapping("/nullpointer")
        public void kasterNullpointer() {
            throw new NullPointerException("hurra");
        }

        @RequestMapping("/applicationException")
        public void kasterApplicationException() {
            throw new ApplicationException("hurra", null, "appex");
        }

        @RequestMapping("/systemException")
        public void kasterSystemException() {
            throw new SystemException("hurra", null, "baksystem");
        }

    }
}
