package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.setProperty;

@Disabled
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SoknadinnsendingConfig.class, OidcConfig.class})
@ActiveProfiles(profiles = {"test", "no-redis"})
public class ApplicationContextTest {

    public static final String ENVIRONMENT_TEST_PROPERTIES = "/environment-test.properties";
    private static SimpleNamingContextBuilder builder;

    @BeforeAll
    public static void beforeClass() throws NamingException {
        load(ENVIRONMENT_TEST_PROPERTIES);

        builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", Mockito.mock(DataSource.class));
        builder.activate();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        builder.deactivate();
    }

    @Test
    void shouldSetupAppContext() {}

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
