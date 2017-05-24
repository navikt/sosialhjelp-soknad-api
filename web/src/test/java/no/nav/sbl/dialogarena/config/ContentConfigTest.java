package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.assertj.core.api.Condition;
import org.glassfish.jersey.jaxb.internal.XmlJaxbElementProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Mock
    KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Before
    public void setup() throws URISyntaxException {
        when(brukerprofilDataDirectory.toURI()).thenReturn(new URI("uri/"));
        when(kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()).thenReturn(new KravdialogInformasjonHolder().getSoknadsKonfigurasjoner());
    }

    @Test
    public void skalReturnereRettAntallBundles(){
        NavMessageSource source = contentConfig.navMessageSource();
        Map<String, NavMessageSource.FileTuple> basenames = source.getBasenames();

        assertThat(source.getFellesBasename()).is(new Condition<NavMessageSource.FileTuple>() {
            @Override
            public boolean matches(NavMessageSource.FileTuple fileTuple) {
                return fileTuple.getRemoteFile().equals("uri/enonic/sendsoknad")
                        && fileTuple.getLocalFile().equals("classpath:content/sendsoknad");
            }
        });
        assertThat(basenames).hasSize(7);
    }

}