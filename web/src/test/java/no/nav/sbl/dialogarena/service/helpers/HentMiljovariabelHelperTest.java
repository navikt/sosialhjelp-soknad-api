package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Miljovariabler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentMiljovariabelHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentMiljovariabelHelper miljovariabel;

    @Mock
    Miljovariabler miljovariabler;

    @Mock
    Map<String, String> miljovariablerMap;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(miljovariabel.getNavn(), miljovariabel);
        when(miljovariabler.hentMiljovariabler()).thenReturn(miljovariablerMap);
        when(miljovariablerMap.get(anyString())).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void henterMiljovariabelFraKey() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{hentMiljovariabel \"dittnav.url\"}}").apply(webSoknad);

        assertThat(compiled).isEqualTo("dittnav.url");
    }
}