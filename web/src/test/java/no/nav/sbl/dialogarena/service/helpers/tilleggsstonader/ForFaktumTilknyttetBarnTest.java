package no.nav.sbl.dialogarena.service.helpers.tilleggsstonader;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktumHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ForFaktumTilknyttetBarnTest {
    private Handlebars handlebars;

    @InjectMocks
    ForFaktumTilknyttetBarn forFaktumTilknyttetBarn;

    @InjectMocks
    ForFaktumHelper forFaktum;

    @Mock
    WebSoknad webSoknad;
    private Faktum barneFaktum;

    @Before
    public void setup(){
        webSoknad = new WebSoknad();
        handlebars = new Handlebars();

        ForFaktumHelper forFaktumHelper = new ForFaktumHelper();
        ForFaktumTilknyttetBarn forFaktumTilknyttetBarn = new ForFaktumTilknyttetBarn();

        handlebars.registerHelper(forFaktumHelper.getNavn(), forFaktumHelper);
        handlebars.registerHelper(forFaktumTilknyttetBarn.getNavn(), forFaktumTilknyttetBarn);
        barneFaktum = new Faktum().medFaktumId(123L).medKey("barn").medValue("en value");
        webSoknad.leggTilFaktum(barneFaktum);
    }

    @Test
    public void skalFinneFaktumSomErTilknyttetBarnet() throws IOException {
        Faktum mittFaktum = new Faktum()
                .medKey("barnepass")
                .medProperty("tilknyttetbarn", "123")
                .medValue("testValue");

        webSoknad.leggTilFaktum(mittFaktum);
        String innhold = handlebars
                .compileInline("{{#forFaktum \"barn\"}}{{#forFaktumTilknyttetBarn \"barnepass\"}}{{value}}{{/forFaktumTilknyttetBarn}}{{/forFaktum}}")
                .apply(webSoknad);
        assertThat(innhold).isEqualTo("testValue");
    }

    @Test
    public void skalIkkeReturnereFaktumSomIkkeErTilknyttetBarnet() throws IOException {
        Faktum mittFaktum = new Faktum()
                .medKey("barnepass")
                .medValue("testValue");

        webSoknad.leggTilFaktum(mittFaktum);
        String innhold = handlebars
                .compileInline("{{#forFaktum \"barn\"}}{{#forFaktumTilknyttetBarn \"barnepass\"}}{{value}}{{/forFaktumTilknyttetBarn}}{{/forFaktum}}")
                .apply(webSoknad);
        assertThat(innhold).doesNotContain("testValue");
    }

    @Test
    public void skalGaaInnIElseContextenOmFaktumetIkkeErTilknyttetBarn() throws IOException {
        Faktum mittFaktum = new Faktum()
                .medKey("barnepass")
                .medValue("testValue");

        webSoknad.leggTilFaktum(mittFaktum);
        String innhold = handlebars
                .compileInline("{{#forFaktum \"barn\"}}{{#forFaktumTilknyttetBarn \"barnepass\"}}{{value}}{{else}}else!{{/forFaktumTilknyttetBarn}}{{/forFaktum}}")
                .apply(webSoknad);
        assertThat(innhold).isEqualTo("else!");
    }

    @Test
    public void skalGaaInnIElseContextenOmFaktumetIkkeFinnes() throws IOException {
        String innhold = handlebars
                .compileInline("{{#forFaktum \"barn\"}}{{#forFaktumTilknyttetBarn \"barnepass\"}}{{value}}{{else}}else!{{/forFaktumTilknyttetBarn}}{{/forFaktum}}")
                .apply(webSoknad);
        assertThat(innhold).isEqualTo("else!");
    }
}