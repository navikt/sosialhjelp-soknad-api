package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ErListeTomHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ErListeTomHelper helper = new ErListeTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalReturnereTomNarListaErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#erListeTom saker}}tom{{else}}ikke tom{{/erListeTom}}").apply(new JsonBostotte());
        assertThat(compiled, is("tom"));
    }

    @Test
    public void skalReturnereIkkeTomNarListaIkkeErTom() throws IOException {
        List<JsonBostotteSak> saker = new ArrayList<>();
        saker.add(new JsonBostotteSak());
        String compiled = handlebars.compileInline("{{#erListeTom saker}}tom{{else}}ikke tom{{/erListeTom}}").apply(new JsonBostotte().withSaker(saker));
        assertThat(compiled, is("ikke tom"));
    }
}