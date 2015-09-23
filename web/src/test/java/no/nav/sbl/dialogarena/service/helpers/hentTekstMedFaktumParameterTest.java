package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class hentTekstMedFaktumParameterTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstMedFaktumParameter hentTekstMedFaktumParameter;

    @Mock
    CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstMedFaktumParameter.getNavn(), hentTekstMedFaktumParameter);
    }

    @Test
    public void kallerCmsTekstMedFaktumValueSomParameter() throws IOException {
        WebSoknad webSoknad = new WebSoknad()
                .medSoknadPrefix("mittprefix")
                .medFaktum(new Faktum()
                        .medKey("faktum.key")
                        .medValue("faktumValue"));

        handlebars.compileInline("{{hentTekstMedFaktumParameter \"cms.key\" \"faktum.key\"}}").apply(webSoknad);

        verify(cmsTekst, times(1)).getCmsTekst("cms.key", new Object[]{"faktumValue"}, "mittprefix");
    }



}