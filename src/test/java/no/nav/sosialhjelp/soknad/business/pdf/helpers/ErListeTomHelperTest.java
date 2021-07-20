package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErListeTomHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        ErListeTomHelper helper = new ErListeTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    void skalReturnereTomNarListaErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#erListeTom saker}}tom{{else}}ikke tom{{/erListeTom}}").apply(new JsonBostotte());
        assertThat(compiled).isEqualTo("tom");
    }

    @Test
    void skalReturnereIkkeTomNarListaIkkeErTom() throws IOException {
        List<JsonBostotteSak> saker = new ArrayList<>();
        saker.add(new JsonBostotteSak());
        String compiled = handlebars.compileInline("{{#erListeTom saker}}tom{{else}}ikke tom{{/erListeTom}}").apply(new JsonBostotte().withSaker(saker));
        assertThat(compiled).isEqualTo("ikke tom");
    }
}