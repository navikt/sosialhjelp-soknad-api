package no.nav.sosialhjelp.soknad.business.pdf.helpers;


import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HvisUtbetalingFinnesHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup(){
        handlebars = new Handlebars();
        HvisUtbetalingFinnesHelper helper = new HvisUtbetalingFinnesHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }
    
    
    @Test
    void skalFinneUtbetalingIOpplysninger() throws IOException{
        final JsonOkonomiopplysninger opplysninger = lagOpplysningerMedEnNavytelseOgEnAnnenUtbetaling();

        String compiled = handlebars.compileInline("{{#hvisUtbetalingFinnes \"navytelse\" }}Utbetalingstypen finnes"
                + "{{else}} Utbetalingstypen finnes ikke{{/hvisUtbetalingFinnes}}").apply(opplysninger);
        
        assertThat(compiled).isEqualTo("Utbetalingstypen finnes");
    }
    
    @Test
    void skalIkkeFinneUtbetalingIOpplysninger() throws IOException{
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
