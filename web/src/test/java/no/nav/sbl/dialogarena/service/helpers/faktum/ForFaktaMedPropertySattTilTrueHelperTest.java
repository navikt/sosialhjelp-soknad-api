package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ForFaktaMedPropertySattTilTrueHelperTest {

    @Mock
    WebSoknad webSoknad;

    private Handlebars handlebars;
    private Faktum faktum1;
    private Faktum faktum2;
    private List<Faktum> fakta;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        ForFaktaMedPropertySattTilTrueHelper helper = new ForFaktaMedPropertySattTilTrueHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
        faktum1 = new Faktum();
        faktum2 = new Faktum();
        fakta = new ArrayList<>();
    }

    @Test
    public void viserInnholdDersomWebSoknadReturnererEttFaktum() throws IOException {
        fakta.add(faktum1);
        when(webSoknad.getFaktaMedKeyOgPropertyLikTrue(anyString(), anyString())).thenReturn(fakta);

        String compiled = handlebars.compileInline("{{#forFaktaMedPropertySattTilTrue \"barn\" \"barnetillegg\" }}" +
                "Dette skal vises.{{else}}Dette skal ikke vises.{{/forFaktaMedPropertySattTilTrue}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Dette skal vises.");
    }

    @Test
    public void viserInnholdDersomWebSoknadReturnererFlereFakta() throws IOException {
        fakta.add(faktum1);
        fakta.add(faktum2);
        when(webSoknad.getFaktaMedKeyOgPropertyLikTrue(anyString(), anyString())).thenReturn(fakta);

        String compiled = handlebars.compileInline("{{#forFaktaMedPropertySattTilTrue \"barn\" \"barnetillegg\" }}" +
                "Innhold som skal vises to ganger.{{/forFaktaMedPropertySattTilTrue}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Innhold som skal vises to ganger.Innhold som skal vises to ganger.");
    }

    @Test
    public void viserIkkeInnholdDersomWebSoknadReturnererTomFaktumliste() throws IOException {
        when(webSoknad.getFaktaMedKeyOgPropertyLikTrue(anyString(), anyString())).thenReturn(fakta);

        String compiled = handlebars.compileInline("{{#forFaktaMedPropertySattTilTrue \"barn\" \"barnetillegg\" }}" +
                "Dette skal ikke vises.{{else}}Dette skal vises.{{/forFaktaMedPropertySattTilTrue}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Dette skal vises.");
    }

    @Test
    public void helperenReturnererEtObjektMedProperties() throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put("sammensattnavn", "Ola Nordmann");
        faktum1.setProperties(properties);
        fakta.add(faktum1);

        when(webSoknad.getFaktaMedKeyOgPropertyLikTrue(anyString(), anyString())).thenReturn(fakta);
        String compiled = handlebars.compileInline("{{#forFaktaMedPropertySattTilTrue \"barn\" \"barnetillegg\" }}" +
                "{{properties.sammensattnavn}}{{/forFaktaMedPropertySattTilTrue}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Ola Nordmann");
    }
}