package no.nav.sbl.dialogarena.websoknad.config;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.setProperty;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadinnsendingConfig.class)
public class ApplicationContextTest {

    @BeforeClass
    public static void beforeClass() throws IOException, NamingException {
        load("/environment-test.properties");
        System.setProperty("no.nav.modig.security.sts.url", "dummyvalue");
        System.setProperty("no.nav.modig.security.systemuser.username", "dummyvalue");
        System.setProperty("no.nav.modig.security.systemuser.password", "");

        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", Mockito.mock(DataSource.class));
        builder.activate();
    }

    @Test
    public void shouldSetupAppContext() {}

    private static Properties load(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream inputStream =  Properties.class.getResourceAsStream(resourcePath)) {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return properties;
    }

}
