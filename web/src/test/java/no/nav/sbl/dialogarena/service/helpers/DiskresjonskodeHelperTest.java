package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.helpers.DiskresjonskodeHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DiskresjonskodeHelperTest {
    private Handlebars handlebars;

    @InjectMocks
    DiskresjonskodeHelper diskresjonskodeHelper;

    @Mock
    HandleBarKjoerer handleBarKjoerer;

    @Mock
    WebSoknad webSoknad;
    private Faktum personaliaFaktum;

    @Before
    public void setup(){
        handlebars = new Handlebars();
        handlebars.registerHelper(diskresjonskodeHelper.getName(), diskresjonskodeHelper.getHelper());
        personaliaFaktum = new Faktum();
        when(webSoknad.getFaktumMedKey(PERSONALIA_FAKTUM_KEY)).thenReturn(personaliaFaktum);
    }

    @Test
    public void skalViseInnholdVedDiskresjonskode6() throws IOException {
        personaliaFaktum.medProperty(DISKRESJONSKODE_PROPERTY, "6");
        String result = handlebars
                .compileInline("{{#hvisKode6Eller7}}diskresjonskode6eller7{{/hvisKode6Eller7}}")
                .apply(webSoknad);
        assertThat(result).isEqualTo("diskresjonskode6eller7");
    }

    @Test
    public void skalViseInnholdVedDiskresjonskode7() throws IOException {
        personaliaFaktum.medProperty(DISKRESJONSKODE_PROPERTY, "7");
        String result = handlebars
                .compileInline("{{#hvisKode6Eller7}}diskresjonskode6eller7{{/hvisKode6Eller7}}")
                .apply(webSoknad);
        assertThat(result).isEqualTo("diskresjonskode6eller7");
    }

    @Test
    public void skalIkkeViseInnholdVedTomDiskresjonskode() throws IOException {
        personaliaFaktum.medProperty(DISKRESJONSKODE_PROPERTY, "");
        String result = handlebars
                .compileInline("{{#hvisKode6Eller7}}diskresjonskode6eller7{{/hvisKode6Eller7}}")
                .apply(webSoknad);
        assertThat(result).isNotEqualTo("diskresjonskode6eller7");
    }

    @Test
    public void skalViseElseInnholdVedTomDiskresjonskode() throws IOException {
        personaliaFaktum.medProperty(DISKRESJONSKODE_PROPERTY, "");
        String result = handlebars
                .compileInline("{{#hvisKode6Eller7}}{{else}}ikkediskresjonskode6eller7{{/hvisKode6Eller7}}")
                .apply(webSoknad);
        assertThat(result).isEqualTo("ikkediskresjonskode6eller7");
    }

    public void skalIkkeFeileUtenDiskresjonskodeProperty() throws IOException {
        String result = handlebars
                .compileInline("{{#hvisKode6Eller7}}{{else}}ikkediskresjonskode6eller7{{/hvisKode6Eller7}}")
                .apply(webSoknad);
        assertThat(result).isEqualTo("ikkediskresjonskode6eller7");
    }

}