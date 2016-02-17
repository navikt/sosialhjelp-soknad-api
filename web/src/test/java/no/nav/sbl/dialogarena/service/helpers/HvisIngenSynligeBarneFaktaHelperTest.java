package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HvisIngenSynligeBarneFaktaHelperTest {

    private Handlebars handlebars;

    private OppsummeringsFaktum synligFaktum;
    private OppsummeringsFaktum skjultFaktum;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        HvisIngenSynligeBarneFaktaHelper helper = new HvisIngenSynligeBarneFaktaHelper();
        handlebars.registerHelper(helper.getNavn(), helper);


        synligFaktum = mock(OppsummeringsFaktum.class);
        when(synligFaktum.erSynlig()).thenReturn(true);

        skjultFaktum = mock(OppsummeringsFaktum.class);
        when(skjultFaktum.erSynlig()).thenReturn(false);

    }

    @Test
    public void ingenSynligeFaktaViserTekst() throws IOException {
        List<OppsummeringsFaktum> fakta = new ArrayList<>();
        fakta.add(skjultFaktum);

        sjekkHelper(fakta, "Ingen synlige fakta");
    }

    @Test
    public void synligeFaktaViserIkkeTekst() throws IOException {
        List<OppsummeringsFaktum> fakta = new ArrayList<>();
        fakta.add(synligFaktum);
        fakta.add(skjultFaktum);

        sjekkHelper(fakta, "");
    }

    private void sjekkHelper(List<OppsummeringsFaktum> fakta, String forventetResultat) throws IOException {
        Context context = Context.newContext(null);
        context.data("fakta", fakta);

        String forFaktaCompiled = handlebars.compileInline("{{#hvisIngenSynligeBarneFakta fakta}}Ingen synlige fakta{{/hvisIngenSynligeBarneFakta}}").apply(context);
        assertThat(forFaktaCompiled).isEqualTo(forventetResultat);
    }

}
