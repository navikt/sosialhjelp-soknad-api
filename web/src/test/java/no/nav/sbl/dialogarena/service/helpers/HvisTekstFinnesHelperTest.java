package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HvisTekstFinnesHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisTekstFinnesHelper hvisTekstFinnesHelper;

    @Mock
    CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisTekstFinnesHelper.getNavn(), hvisTekstFinnesHelper);
    }

    @Test
    public void trueOmTekstFinnes() throws IOException {
        when(cmsTekst.finnesTekst(anyString(), anyString(), any(Locale.class))).thenReturn(true);
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("true");
    }
    @Test
    public void falseOmTekstIkkeFinnes() throws IOException {
        when(cmsTekst.finnesTekst(anyString(), anyString(), any(Locale.class))).thenReturn(false);
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("false");
    }

}