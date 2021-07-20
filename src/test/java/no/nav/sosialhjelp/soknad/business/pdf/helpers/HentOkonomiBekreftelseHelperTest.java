package no.nav.sosialhjelp.soknad.business.pdf.helpers;


import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HentOkonomiBekreftelseHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup(){
        handlebars = new Handlebars();
        HentOkonomiBekreftelseHelper helper = new HentOkonomiBekreftelseHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }
    
    
    @Test
    void hentOkonomiBekreftelseSomLiggerIListe() throws IOException{
        final JsonOkonomiopplysninger opplysninger = lagOpplysningerMedBostotteBekreftelse(true);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        
        assertThat(compiled).isEqualTo("Verdi: true");
    }

    @Test
    void bekreftelseMedVerdiLikNull() throws IOException{
        
        final JsonOkonomiopplysninger opplysninger = lagOpplysningerMedBostotteBekreftelse(false);
        
        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        
        assertThat(compiled).isEqualTo("Verdi: ");
    }
    
    @Test
    void bekreftelseListeErNull() throws IOException{
        
        final JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger();
        opplysninger.setBekreftelse(null);

        String compiled = handlebars.compileInline("{{#hentOkonomiBekreftelse \"bostotte\" }}Verdi: {{verdi}}{{/hentOkonomiBekreftelse}}").apply(opplysninger);
        assertThat(compiled).isBlank();
    }
    
    private JsonOkonomiopplysninger lagOpplysningerMedBostotteBekreftelse(boolean bostotteBekreftelseSkalHaVerdiLikTrue) {
        final JsonOkonomibekreftelse bostotteBekreftelse = new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Søkt eller mottatt bostøtte fra Husbanken.")
                .withType(BOSTOTTE);
        
        if (bostotteBekreftelseSkalHaVerdiLikTrue) {
            bostotteBekreftelse.setVerdi(Boolean.TRUE);
        }
        
        final List<JsonOkonomibekreftelse> bekreftelser = new ArrayList<JsonOkonomibekreftelse>();
        bekreftelser.add(bostotteBekreftelse);

        return new JsonOkonomiopplysninger()
                .withBekreftelse(bekreftelser);
    }
}
