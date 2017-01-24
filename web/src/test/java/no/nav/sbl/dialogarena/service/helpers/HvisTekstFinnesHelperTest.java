package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HvisTekstFinnesHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisTekstFinnesHelper hvisTekstFinnesHelper;

    @Mock
    CmsTekst cmsTekst;

    @Mock
    KravdialogInformasjonHolder kravdialogInformasjonHolder;


    @Before
    public void setup() {
        KravdialogInformasjon kravdialogInformasjon = mock(KravdialogInformasjon.class);
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getBundleName()).thenReturn("bundlename");

        handlebars = new Handlebars();
        handlebars.registerHelper(hvisTekstFinnesHelper.getNavn(), hvisTekstFinnesHelper);
    }

    @Test
    public void trueOmTekstFinnes() throws IOException {
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("hei hei");
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("true");
    }

    @Test
    public void falseOmTekstIkkeFinnes() throws IOException {
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn(null);
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{#hvisTekstFinnes \"test\"}}true{{else}}false{{/hvisTekstFinnes}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("false");
    }

}