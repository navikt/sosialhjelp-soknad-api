package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ContextConfiguration(classes = {SoknadRessurs.class, MockMvcSoknadRessursTestConfig.class})
@WebAppConfiguration()
public class MockMvcSoknadRessursEndpointIT extends AbstractJUnit4SpringContextTests {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    protected MockServletContext mockServletContext;

    @Autowired
    @InjectMocks
    private SoknadRessurs soknadRessurs;

    private MockMvc mockMvc;

    public static final String ANNEN_BRUKER = "12345679811";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
//                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void nektetTilgang_opprettEttersendelse() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .get("/soknader/opprettSoknad").accept(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("ADMIN"));

//        Response response = soknadTester.sendsoknadResource(url, webTarget -> webTarget
//                .queryParam("fnr", ANNEN_BRUKER)
//                .queryParam("ettersendTil", soknadTester.getBrukerBehandlingId() )) //fake annen bruker, se FakeLoginFilter
//                .buildPost(null)
//                .invoke();
//        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void nektetTilgang_url1() throws Exception {
        ServletContext servletContext = wac.getServletContext();

        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        for (String beanName : wac.getBeanDefinitionNames()) {
            if (beanName.equalsIgnoreCase("soknadRessurs")) {
                System.out.println("Bean Name: " + beanName);
                System.out.println("Bean " + wac.getBean(beanName));
            }
        }

        mockMvc.perform(
                MockMvcRequestBuilders.post("/sosialhjelp/soknader/opprettSoknad")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("ADMIN"));
    }
    @Test
    public void nektetTilgang_url2() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/soknader/opprettSoknad")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("ADMIN"));
    }
    @Test
    public void nektetTilgang_url3() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/opprettSoknad")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("ADMIN"));
    }
    @Test
    public void nektetTilgang_url4() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("ADMIN"));
    }
}

