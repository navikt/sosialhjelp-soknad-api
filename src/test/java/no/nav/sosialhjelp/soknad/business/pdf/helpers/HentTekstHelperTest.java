package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;

import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HentTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstHelper hentTekstHelper;

    @Mock
    CmsTekst cmsTekst;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstHelper.getNavn(), hentTekstHelper);
        when(cmsTekst.getCmsTekst(anyString(), any(Object[].class), anyString(), anyString(), any(Locale.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    public void henterTekstFraCmsTekst() throws IOException {
        String compiled = handlebars.compileInline("{{hentTekst \"test\"}}").apply(new Object());

        assertThat(compiled).isEqualTo("test");
    }

    @Test
    public void senderParametereTilCmsTekst() throws IOException {
        handlebars.compileInline("{{hentTekst \"test\" \"param1\" \"param2\"}}").apply(new Object());

        verify(cmsTekst, atLeastOnce()).getCmsTekst("test", new Object[]{"param1", "param2"}, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, toLocale("nb_NO"));
    }


}