package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FormaterLangDatoHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FormaterLangDatoHelper helper = new FormaterLangDatoHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseDagerFormat() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix");
        String innhold = handlebars.compileInline("{{formaterLangDato \"2015-10-03\"}}, {{formaterLangDato \"2015-11-15\"}}").apply(webSoknad);
        assertThat(innhold).isEqualTo("3. oktober 2015, 15. november 2015");
    }

    @Test
    public void visManedsNavnPaEngelsk() throws IOException {
        Faktum sprakFaktum = new Faktum().medFaktumId(123L).medKey("skjema.sprak").medValue("en");
        WebSoknad webSoknad = new WebSoknad().medSoknadPrefix("mittprefix").medFaktum(sprakFaktum);
        String innhold = handlebars.compileInline("{{formaterLangDato \"2015-10-03\"}}, {{formaterLangDato \"2015-11-15\"}}").apply(webSoknad);
        assertThat(innhold).isEqualTo("3. October 2015, 15. November 2015");
    }


}