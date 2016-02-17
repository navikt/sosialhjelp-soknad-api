package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HentTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstHelper hentTekstHelper;

    @Mock
    CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstHelper.getNavn(), hentTekstHelper);
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), any(Locale.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void henterTekstFraCmsTekst() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String compiled = handlebars.compileInline("{{hentTekst \"test\"}}").apply(webSoknad);

        assertThat(compiled).isEqualTo("test");
    }

    @Test
    public void senderParametereTilCmsTekst() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");

        handlebars.compileInline("{{hentTekst \"test\" \"param1\" \"param2\"}}").apply(webSoknad);

        verify(cmsTekst, atLeastOnce()).getCmsTekst("test", new Object[]{"param1", "param2"}, "mittprefix", toLocale("nb_NO"));
    }

}