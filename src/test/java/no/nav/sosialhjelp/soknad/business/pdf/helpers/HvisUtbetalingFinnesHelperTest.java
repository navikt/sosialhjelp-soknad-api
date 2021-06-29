package no.nav.sosialhjelp.soknad.business.pdf.helpers;


import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static org.assertj.core.api.Assertions.assertThat;

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
        final JsonOkonomiopplysninger opplysninger = lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling();

        String compiled = handlebars.compileInline("{{#hvisUtbetalingFinnes \"navytelse\" }}Utbetalingstypen finnes"
                + "{{else}} Utbetalingstypen finnes ikke{{/hvisUtbetalingFinnes}}").apply(opplysninger);
        
        assertThat(compiled).isEqualTo("Utbetalingstypen finnes");
    }
    
    @Test
    public void skalIkkeFinneUtbetalingIOpplysninger() throws IOException{
        final JsonOkonomiopplysninger opplysninger = lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling();

        String compiled = handlebars.compileInline("{{#hvisUtbetalingFinnes \"tullepenger\" }}Utbetalingstypen finnes"
                + "{{else}}Utbetalingstypen finnes ikke{{/hvisUtbetalingFinnes}}").apply(opplysninger);
        
        assertThat(compiled).isEqualTo("Utbetalingstypen finnes ikke");
    }


    private JsonOkonomiopplysninger lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling() {
        final JsonOkonomiOpplysningUtbetaling navUtbetaling = new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withTittel("Dagpenger")
                .withType(UTBETALING_NAVYTELSE);
        
        final JsonOkonomiOpplysningUtbetaling annenUtbetaling = new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.BRUKER)
                .withTittel("Bitcoin gevinst")
                .withType("crypto");
        
        final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<JsonOkonomiOpplysningUtbetaling>();
        utbetalinger.add(navUtbetaling);
        utbetalinger.add(annenUtbetaling);

        return new JsonOkonomiopplysninger()
                .withUtbetaling(utbetalinger);
    }
    
}
