package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentConfigTest extends ApplicationContextTest {

    @Mock
    File brukerprofilDataDirectory;

    @InjectMocks
    ContentConfig contentConfig;

    @Before
    public void setup() throws URISyntaxException {
        when(brukerprofilDataDirectory.toURI()).thenReturn(new URI("uri/"));
    }

    @Test
    public void skalReturnereRettAntallBundles(){
        NavMessageSource source = contentConfig.navMessageSource();
        Map<String, String> basenames = source.getBasenames();
        assertThat(basenames).hasSize(1);
    }

}