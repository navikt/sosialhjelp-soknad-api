package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilsynskategorier;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;

public class TilsynBarnepassTilXml implements Transformer<WebSoknad, TilsynsutgifterBarn> {
    private  static final String PERIODE = "barnepass.periode";
    private  static final String UTBETALINGSDATO = "barnepass.utbetalingsdato";
    private  static final String BARN = "barn";
    private  static final String ANDREFORELDER = "andreforelder";
    private  static final String SOKERBARNEPASS = "sokerOmBarnepass";
    private  TilsynsutgifterBarn barnepass = new TilsynsutgifterBarn();

    @Override
    public TilsynsutgifterBarn transform(WebSoknad soknad) {
        barnepass.setPeriode(extractValue(soknad.getFaktumMedKey(PERIODE), Periode.class));
        barnSomDetSokesBarnepassOm(soknad);
        utbetalingsdato(soknad);

        return barnepass;
    }

    private void utbetalingsdato(WebSoknad soknad) {
        Faktum utbetalingsdatoFaktum = soknad.getFaktumMedKey(UTBETALINGSDATO);

        if(utbetalingsdatoFaktum != null && utbetalingsdatoFaktum.hasValue()) {
            barnepass.setOensketUtbetalingsdag(new BigInteger(utbetalingsdatoFaktum.getValue()));
        }
    }

    private void barnSomDetSokesBarnepassOm(WebSoknad soknad) {
        List<Faktum> alleBarn = soknad.getFaktaMedKey(BARN);

        for (Faktum barn : alleBarn) {
            if("true".equals(barn.getProperties().get(SOKERBARNEPASS))) {
                barnepass.getBarn().add(extractValue(barn, Barn.class));
                barnepass.setAnnenForsoergerperson(extractValue(barn, String.class, ANDREFORELDER));//TODO - er ikke støtte for å sende inn per barn
                barnepass.setTilsynskategori(extractValue(barn, Tilsynskategorier.class));  //TODO - er ikke støtte for å sende inn per barn
            }
        }
    }
}
