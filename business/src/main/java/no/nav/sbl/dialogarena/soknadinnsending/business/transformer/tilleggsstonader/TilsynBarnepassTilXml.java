package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumPredicates.harValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;

public class TilsynBarnepassTilXml implements Transformer<WebSoknad, TilsynsutgifterBarn> {
    private static final String PERIODE = "barnepass.periode";
    private static final String UTBETALINGSDATO = "barnepass.utbetalingsdato";
    private static final String SOKERBARNEPASS = "barnepass.sokerbarnepass";
//    private static final String BARNEPASS_ANDREFORELDER = "barnepass.andreforelder";
//    private static final String BARNEPASS_TYPER_DAGMAMMA = "barnepass.typer.dagmamma";
//    private static final String BARNEPASS_TYPER_BARNEHAGE = "barnepass.typer.barnehage";
//    private static final String BARNEPASS_TYPER_PRIVAT = "barnepass.typer.privat";
    private TilsynsutgifterBarn tilsynsutgifterBarn = new TilsynsutgifterBarn();

    @Override
    public TilsynsutgifterBarn transform(WebSoknad soknad) {
        tilsynsutgifterBarn.setPeriode(extractValue(soknad.getFaktumMedKey(PERIODE), Periode.class));
        barnSomDetSokesBarnepassOm(soknad);
        tilsynsutgifterBarn.setOensketUtbetalingsdag(extractValue(soknad.getFaktumMedKey(UTBETALINGSDATO), BigInteger.class));
        return tilsynsutgifterBarn;
    }

    private void barnSomDetSokesBarnepassOm(WebSoknad soknad) {
        List<Faktum> sokerBarnepassBarn = on(soknad.getFaktaMedKey(SOKERBARNEPASS)).filter(harValue("true")).collect();
        for (Faktum barnepass : sokerBarnepassBarn) {
            Faktum barn = soknad.finnFaktum(Long.valueOf(barnepass.getProperties().get("tilknyttetbarn")));
            if (barn != null) {
                tilsynsutgifterBarn.getBarn().add(extractValue(barn, Barn.class));
//                String annenForelder = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_ANDREFORELDER, barnepass.getFaktumId()), String.class);
//                Boolean dagmamma = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_DAGMAMMA, barnepass.getFaktumId()), Boolean.class);
//                Boolean barnehage = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_BARNEHAGE, barnepass.getFaktumId()), Boolean.class);
//                Boolean privat = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_PRIVAT, barnepass.getFaktumId()), Boolean.class);
                //Legg på barn når det kommer.
                //barn.setTilsynskategori(extractValue(barn, Tilsynskategorier.class));  //TODO - er ikke støtte for å sende inn per barn
                //barn.setAnnenForelder/annenForelder
            }
        }
    }
}
