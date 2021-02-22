package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ContentConfigTest extends ApplicationContextTest {

    @Mock
    File brukerprofilDataDirectory;

    @InjectMocks
    ContentConfig contentConfig;

    @Test
    public void skalReturnereRettAntallBundles(){
        NavMessageSource source = contentConfig.navMessageSource();
        Map<String, String> basenames = source.getBasenames();
        assertThat(basenames).hasSize(1);
    }

}