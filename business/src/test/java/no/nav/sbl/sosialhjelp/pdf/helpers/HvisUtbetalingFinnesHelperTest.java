package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
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
public class HvisUtbetalingFinnesHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup(){
        handlebars = new Handlebars();
        HvisUtbetalingFinnesHelper helper = new HvisUtbetalingFinnesHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }
    
    
    @Test
    public void skalFinneUtbetalingIOpplysninger() throws IOException{
        JsonOkonomiopplysninger opplysninger = lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling();

        String compiled = handlebars.compileInline("{{#hvisUtbetalingFinnes \"navytelse\" }}Utbetalingstypen finnes"
                + "{{else}} Utbetalingstypen finnes ikke{{/hvisUtbetalingFinnes}}").apply(opplysninger);
        
        assertThat(compiled, equalTo("Utbetalingstypen finnes"));
    }
    
    @Test
    public void skalIkkeFinneUtbetalingIOpplysninger() throws IOException{
        JsonOkonomiopplysninger opplysninger = lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling();

        String compiled = handlebars.compileInline("{{#hvisUtbetalingFinnes \"tullepenger\" }}Utbetalingstypen finnes"
                + "{{else}}Utbetalingstypen finnes ikke{{/hvisUtbetalingFinnes}}").apply(opplysninger);
        
        assertThat(compiled, equalTo("Utbetalingstypen finnes ikke"));
    }


    private JsonOkonomiopplysninger lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling() {
        JsonOkonomiOpplysningUtbetaling navUtbetaling = new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withTittel("Dagpenger")
                .withType("navytelse");
        
        JsonOkonomiOpplysningUtbetaling annenUtbetaling = new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Bitcoin gevinst")
                .withType("crypto");
        
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<JsonOkonomiOpplysningUtbetaling>();
        utbetalinger.add(navUtbetaling);
        utbetalinger.add(annenUtbetaling);
        
        JsonOkonomiopplysninger opplysninger = new JsonOkonomiopplysninger()
                .withUtbetaling(utbetalinger);
        return opplysninger;
    }
    
}
