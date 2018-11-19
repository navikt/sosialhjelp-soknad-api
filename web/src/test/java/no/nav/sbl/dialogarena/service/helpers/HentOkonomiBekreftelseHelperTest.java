package no.nav.sbl.dialogarena.service.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.jknack.handlebars.Handlebars;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;

@RunWith(MockitoJUnitRunner.class)
public class HentOkonomiBekreftelseHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup(){
        handlebars = new Handlebars();
        HentOkonomiBekreftelseHelper helper = new HentOkonomiBekreftelseHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }
    
    
    @Test
    public void hentOkonomiBekreftelseSomLiggerIListe() throws IOException{
        final JsonOkonomibekreftelse bostotteBekreftelse = new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Søkt eller mottatt bostøtte fra Husbanken.")
                .withType("bostotte")
                .withVerdi(Boolean.TRUE);
        
        final List<JsonOkonomibekreftelse> bekreftelser = new ArrayList<JsonOkonomibekreftelse>();
        bekreftelser.add(bostotteBekreftelse);
        
        final JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        assertThat(compiled).isEqualTo("Verdi: true");
    }
    
    @Test
    public void bekreftelseMedVerdiLikNull() throws IOException{
        
        final JsonOkonomibekreftelse bostotteBekreftelse = new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Søkt eller mottatt bostøtte fra Husbanken.")
                .withType("bostotte");
        
        final List<JsonOkonomibekreftelse> bekreftelser = new ArrayList<JsonOkonomibekreftelse>();
        bekreftelser.add(bostotteBekreftelse);
        
        final JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);
        
        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        assertThat(compiled).isEqualTo("Verdi: ");
    }
    
    @Test
    public void bekreftelseListeErNull() throws IOException{
        
        final JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger();
        opplysninger.setBekreftelse(null);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        assertThat(compiled).isEqualTo("");
    }
    
}
