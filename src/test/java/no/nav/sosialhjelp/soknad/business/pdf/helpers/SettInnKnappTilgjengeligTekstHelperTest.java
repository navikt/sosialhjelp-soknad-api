package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettInnKnappTilgjengeligTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    private SettInnKnappTilgjengeligTekstHelper settInnKnappTilgjengeligTekstHelper;

    @Mock
    private CmsTekst cmsTekst;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(settInnKnappTilgjengeligTekstHelper.getNavn(), settInnKnappTilgjengeligTekstHelper);
    }

    @Test
    void skalHenteKnapptekstMedTittel() throws IOException {
        when(cmsTekst.getCmsTekst(eq("testTekst"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Lorem ipsum");

        String compiled = handlebars.compileInline("{{{settInnKnappTilgjengeligTekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled).contains("Knapp tilgjengelig:", "Lorem ipsum");
    }

    @Test
    void skalReturnereTomStrengHvisIkkeKnapptekstFinnes() throws IOException {
        String compiled = handlebars.compileInline("{{{settInnKnappTilgjengeligTekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled).isBlank();
    }

}