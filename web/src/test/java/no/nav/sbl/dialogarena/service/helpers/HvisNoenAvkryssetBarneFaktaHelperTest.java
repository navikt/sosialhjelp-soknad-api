package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HvisNoenAvkryssetBarneFaktaHelperTest {

    private Handlebars handlebars;

    private OppsummeringsFaktum avkryssetFaktum;
    private OppsummeringsFaktum ikkeAvkryssetFaktum;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        HvisNoenAvkryssetBarneFaktaHelper helper = new HvisNoenAvkryssetBarneFaktaHelper();
        handlebars.registerHelper(helper.getNavn(), helper);


        avkryssetFaktum = mock(OppsummeringsFaktum.class);
        when(avkryssetFaktum.value()).thenReturn("true");

        ikkeAvkryssetFaktum = mock(OppsummeringsFaktum.class);
        when(ikkeAvkryssetFaktum.value()).thenReturn("");

    }

    @Test
    public void ingenAvkryssetFaktaViserIkkeTekst() throws IOException {
        List<OppsummeringsFaktum> fakta = new ArrayList<>();
        fakta.add(ikkeAvkryssetFaktum);
        fakta.add(ikkeAvkryssetFaktum);

        sjekkHelper(fakta, "");
    }

    @Test
    public void avkryssetFaktaViserTekst() throws IOException {
        List<OppsummeringsFaktum> fakta = new ArrayList<>();
        fakta.add(ikkeAvkryssetFaktum);
        fakta.add(avkryssetFaktum);

        sjekkHelper(fakta, "Ingen synlige fakta");
    }

    private void sjekkHelper(List<OppsummeringsFaktum> fakta, String forventetResultat) throws IOException {
        Context context = Context.newContext(null);
        context.data("fakta", fakta);

        String forFaktaCompiled = handlebars.compileInline("{{#hvisNoenAvkryssetBarneFakta fakta}}Ingen synlige fakta{{/hvisNoenAvkryssetBarneFakta}}").apply(context);
        assertThat(forFaktaCompiled).isEqualTo(forventetResultat);
    }

}
