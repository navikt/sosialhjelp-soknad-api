package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisKunStudentHelperTest {
    private Handlebars handlebars;

    WebSoknad webSoknad;

    private static final String BARE_STUDENT = "bareStudent";
    private static final String IKKE_BARE_STUDENT = "ikkeBareStudent";

    @Before
    public void setup() {
        HvisKunStudentHelper helper = new HvisKunStudentHelper();

        handlebars = new Handlebars();
        handlebars.registerHelper(helper.getNavn(), helper);

        webSoknad = new WebSoknad();
        webSoknad
                .leggTilFaktum(nyttFaktum("navaerendeSituasjon.iArbeid"))
                .leggTilFaktum(nyttFaktum("navaerendeSituasjon.sykmeldt"))
                .leggTilFaktum(nyttFaktum("navaerendeSituasjon.arbeidsledig"))
                .leggTilFaktum(nyttFaktum("navaerendeSituasjon.forstegangstjeneste"))
                .leggTilFaktum(nyttFaktum("navaerendeSituasjon.annet"));
    }

    @Test
    public void brukerHarBareStudentStatus() throws IOException {
        assertThat(innholdTilStudentSjekk()).isEqualTo(BARE_STUDENT);
    }

    @Test
    public void brukerHarAnnenStatusEnnStudent() throws IOException {
        webSoknad.getFaktumMedKey("navaerendeSituasjon.sykmeldt").setValue("true");
        assertThat(innholdTilStudentSjekk()).isEqualTo(IKKE_BARE_STUDENT);
    }

    private String innholdTilStudentSjekk() throws IOException {
        return handlebars
                .compileInline("{{#hvisKunStudent}}" + BARE_STUDENT + "{{else}}" + IKKE_BARE_STUDENT + "{{/hvisKunStudent}}")
                .apply(webSoknad);
    }

    private Faktum nyttFaktum(String key) {
        return new Faktum().medKey(key);
    }
}