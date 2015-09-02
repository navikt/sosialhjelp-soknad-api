package no.nav.sbl.dialogarena.service.helpers.tilleggsstonader;

import com.github.jknack.handlebars.*;
import no.nav.sbl.dialogarena.service.helpers.*;
import no.nav.sbl.dialogarena.service.helpers.faktum.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import java.io.*;

import static no.nav.sbl.dialogarena.service.helpers.DiskresjonskodeHelper.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        handlebars.registerHelper(forFaktum.getNavn(), forFaktum.getHelper());
        handlebars.registerHelper(forFaktumTilknyttetBarn.getNavn(), forFaktumTilknyttetBarn.getHelper());
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