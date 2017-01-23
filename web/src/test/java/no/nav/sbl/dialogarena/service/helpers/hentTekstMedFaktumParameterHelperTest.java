package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
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

import static org.apache.commons.lang.LocaleUtils.toLocale;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class hentTekstMedFaktumParameterHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstMedFaktumParameterHelper hentTekstMedFaktumParameterHelper;

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
        handlebars.registerHelper(hentTekstMedFaktumParameterHelper.getNavn(), hentTekstMedFaktumParameterHelper);
    }

    @Test
    public void kallerCmsTekstMedFaktumValueSomParameter() throws IOException {
        WebSoknad webSoknad = new WebSoknad()
                .medSoknadPrefix("mittprefix")
                .medFaktum(new Faktum()
                        .medKey("faktum.key")
                        .medValue("faktumValue"));

        handlebars.compileInline("{{hentTekstMedFaktumParameter \"cms.key\" \"faktum.key\"}}").apply(webSoknad);

        verify(cmsTekst, times(1)).getCmsTekst("cms.key", new Object[]{"faktumValue"}, "mittprefix", "bundlename", toLocale("nb_NO"));
    }
}