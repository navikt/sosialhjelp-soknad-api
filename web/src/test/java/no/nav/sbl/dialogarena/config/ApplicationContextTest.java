package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.oidc.OidcConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.getProperties;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.MockUtil.DEFAULT_MOCK_TILLATT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.MockUtil.TILLATMOCK_PROPERTY;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadinnsendingConfig.class, OidcConfig.class})
public class ApplicationContextTest {

    public static final String ENVIRONMENT_TEST_PROPERTIES = "/environment-test.properties";
    private static SimpleNamingContextBuilder builder;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        load(ENVIRONMENT_TEST_PROPERTIES);
        getProperties().setProperty(TILLATMOCK_PROPERTY, DEFAULT_MOCK_TILLATT);

        builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", Mockito.mock(DataSource.class));
        builder.activate();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        builder.deactivate();
    }

    @Test
    public void shouldSetupAppContext() {}

    private static Properties load(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream =  ApplicationContextTest.class.getResourceAsStream(resourcePath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return properties;
    }

}
