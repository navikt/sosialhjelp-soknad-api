package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
        JsonOkonomiopplysninger opplysninger = lagOpplysningerMedBostotteBekreftelse(true);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        
        assertThat(compiled, equalTo("Verdi: true"));
    }

    @Test
    public void bekreftelseMedVerdiLikNull() throws IOException{
        
        JsonOkonomiopplysninger opplysninger = lagOpplysningerMedBostotteBekreftelse(false);
        
        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        
        assertThat(compiled, equalTo("Verdi: "));
    }
    
    @Test
    public void bekreftelseListeErNull() throws IOException{
        
        JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger();
        opplysninger.setBekreftelse(null);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        assertThat(compiled, equalTo(""));
    }
    
    private JsonOkonomiopplysninger lagOpplysningerMedBostotteBekreftelse(boolean bostotteBekreftelseSkalHaVerdiLikTrue) {
        JsonOkonomibekreftelse bostotteBekreftelse = new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Søkt eller mottatt bostøtte fra Husbanken.")
                .withType("bostotte");
        
        if (bostotteBekreftelseSkalHaVerdiLikTrue) {
            bostotteBekreftelse.setVerdi(Boolean.TRUE);
        }
        
        List<JsonOkonomibekreftelse> bekreftelser = new ArrayList<JsonOkonomibekreftelse>();
        bekreftelser.add(bostotteBekreftelse);
        
        JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);
        return opplysninger;
    }
}
