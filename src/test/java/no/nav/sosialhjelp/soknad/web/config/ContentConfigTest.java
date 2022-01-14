package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@ExtendWith(MockitoExtension.class)
class ContentConfigTest extends ApplicationContextTest {

    @InjectMocks
    ContentConfig contentConfig;

    @Test
    void skalReturnereRettAntallBundles(){
        NavMessageSource source = contentConfig.navMessageSource();
        Map<String, String> basenames = source.getBasenames();
        assertThat(basenames).hasSize(1);
    }

}