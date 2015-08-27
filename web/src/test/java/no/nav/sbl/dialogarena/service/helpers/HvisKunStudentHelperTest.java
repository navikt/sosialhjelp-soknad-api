package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisKunStudentHelperTest {
    private Handlebars handlebars;

    HvisKunStudentHelper hvisKunStudentHelper;

    WebSoknad webSoknad;

    private Faktum iArbeidFaktum;
    private Faktum sykmeldtFaktum;
    private Faktum arbeidsledigFaktum;
    private Faktum forstegangstjenesteFaktum;
    private Faktum annetFaktum;

    private static final String BARE_STUDENT = "bareStudent";
    private static final String IKKE_BARE_STUDENT = "ikkeBareStudent";

    @Before
    public void setup() {
        hvisKunStudentHelper = new HvisKunStudentHelper();

        handlebars = new Handlebars();
        handlebars.registerHelper(hvisKunStudentHelper.getNavn(), hvisKunStudentHelper.getHelper());

        faktumMock();

        webSoknad = new WebSoknad();
        webSoknad
                .leggTilFaktum(iArbeidFaktum)
                .leggTilFaktum(sykmeldtFaktum)
                .leggTilFaktum(arbeidsledigFaktum)
                .leggTilFaktum(forstegangstjenesteFaktum)
                .leggTilFaktum(annetFaktum);
    }

    @Test
    public void brukerHarBareStudentStatus() throws IOException {
        assertThat(innholdTilStudentSjekk()).isEqualTo(BARE_STUDENT);
    }

    @Test
    public void brukerHarAnnenStatusEnnStudent() throws IOException {
        sykmeldtFaktum.setValue("true");
        assertThat(innholdTilStudentSjekk()).isEqualTo(IKKE_BARE_STUDENT);
    }

    private String innholdTilStudentSjekk() throws IOException {
        return handlebars
                .compileInline("{{#hvisKunStudent}}" + BARE_STUDENT + "{{else}}" + IKKE_BARE_STUDENT + "{{/hvisKunStudent}}")
                .apply(webSoknad);
    }

    private void faktumMock() {
        iArbeidFaktum = new Faktum()
                .medKey("navaerendeSituasjon.iArbeid");

        sykmeldtFaktum = new Faktum()
                .medKey("navaerendeSituasjon.sykmeldt");

        arbeidsledigFaktum = new Faktum()
                .medKey("navaerendeSituasjon.arbeidsledig");

        forstegangstjenesteFaktum = new Faktum()
                .medKey("navaerendeSituasjon.forstegangstjeneste");

        annetFaktum = new Faktum()
                .medKey("navaerendeSituasjon.annet");
    }
}