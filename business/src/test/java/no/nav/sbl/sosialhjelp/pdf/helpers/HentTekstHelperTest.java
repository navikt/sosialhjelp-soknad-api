package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.junit.Assert;
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
import static org.hamcrest.CoreMatchers.is;
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
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void henterTekstFraCmsTekst() throws IOException {
        String compiled = handlebars.compileInline("{{hentTekst \"test\"}}").apply(new Object());

        Assert.assertThat(compiled, is("test"));
    }

    @Test
    public void senderParametereTilCmsTekst() throws IOException {
        handlebars.compileInline("{{hentTekst \"test\" \"param1\" \"param2\"}}").apply(new Object());

        verify(cmsTekst, atLeastOnce()).getCmsTekst("test", new Object[]{"param1", "param2"}, "mittprefix", "bundlename", toLocale("nb_NO"));
    }


}