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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HvisKunStudentHelperTest {
    private Handlebars handlebars;

    @InjectMocks
    HvisKunStudentHelper hvisKunStudentHelper;

    @Mock
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
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisKunStudentHelper.getNavn(), hvisKunStudentHelper.getHelper());

        faktumMock();

        when(webSoknad.getFaktumMedKey(iArbeidFaktum.getKey())).thenReturn(iArbeidFaktum);
        when(webSoknad.getFaktumMedKey(sykmeldtFaktum.getKey())).thenReturn(sykmeldtFaktum);
        when(webSoknad.getFaktumMedKey(arbeidsledigFaktum.getKey())).thenReturn(arbeidsledigFaktum);
        when(webSoknad.getFaktumMedKey(forstegangstjenesteFaktum.getKey())).thenReturn(forstegangstjenesteFaktum);
        when(webSoknad.getFaktumMedKey(annetFaktum.getKey())).thenReturn(annetFaktum);
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
                .compileInline("{{#hvisKunStudent}}"+ BARE_STUDENT +"{{else}}"+ IKKE_BARE_STUDENT +"{{/hvisKunStudent}}")
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