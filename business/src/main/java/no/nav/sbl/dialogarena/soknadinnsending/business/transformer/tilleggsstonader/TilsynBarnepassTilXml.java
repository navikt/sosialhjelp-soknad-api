package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.AarsakTilBarnepass;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilsynskategorier;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumPredicates.harValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers.extractValue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class TilsynBarnepassTilXml implements Transformer<WebSoknad, TilsynsutgifterBarn> {
    public static final String BARNEPASS_ANDREFORELDER = "barnepass.andreforelder";
    public static final String BARNEPASS_TYPER_DAGMAMMA = "barnepass.typer.dagmamma";
    public static final String BARNEPASS_TYPER_BARNEHAGE = "barnepass.typer.barnehage";
    public static final String BARNEPASS_TYPER_PRIVAT = "barnepass.typer.privat";
    public static final String BARNEPASS_FOLLFORT_FJERDE = "barnepass.fjerdeklasse";
    public static final List<String> BARNEPASS_AARSAKER = Arrays.asList("barnepass.fjerdeklasse.langvarig", "barnepass.fjerdeklasse.trengertilsyn", "barnepass.fjerdeklasse.ingen");
    private static final String PERIODE = "barnepass.periode";
    private static final String UTBETALINGSDATO = "barnepass.utbetalingsdato";
    private static final String SOKERBARNEPASS = "barnepass.sokerbarnepass";
    private final MessageSource navMessageSource;
    private TilsynsutgifterBarn tilsynsutgifterBarn = new TilsynsutgifterBarn();

    public TilsynBarnepassTilXml(MessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
    }

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
                Barn stofoBarn = extractValue(barn, Barn.class);
                Faktum fulfortFjerde = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_FOLLFORT_FJERDE, barnepass.getFaktumId());
                stofoBarn.setHarFullfoertFjerdeSkoleaar(extractValue(fulfortFjerde, Boolean.class));
                List<AarsakTilBarnepass> aarsakTilBarnepasses = aarsaker(soknad, fulfortFjerde.getFaktumId());
                stofoBarn.setAarsakTilBarnepass(aarsakTilBarnepasses.isEmpty() ? null : aarsakTilBarnepasses.get(0));//TODO: Sttte for flere
                tilsynsutgifterBarn.getBarn().add(stofoBarn);
                String annenForelder = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_ANDREFORELDER, barnepass.getFaktumId()), String.class);
                Boolean dagmamma = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_DAGMAMMA, barnepass.getFaktumId()), Boolean.class);
                Boolean barnehage = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_BARNEHAGE, barnepass.getFaktumId()), Boolean.class);
                Boolean privat = extractValue(soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER_PRIVAT, barnepass.getFaktumId()), Boolean.class);
                stofoBarn.setTilsynskategori(extractValue(new Faktum()
                        .medProperty("dagmamma", "" + dagmamma)
                        .medProperty("barnehage", "" + barnehage)
                        .medProperty("privat", "" + privat)
                        , Tilsynskategorier.class));
                tilsynsutgifterBarn.setAnnenForsoergerperson(annenForelder);
            }
        }
    }

    private List<AarsakTilBarnepass> aarsaker(WebSoknad soknad, Long parentFaktumId) {
        List<AarsakTilBarnepass> result = new ArrayList<>();
        for (String faktumKey : BARNEPASS_AARSAKER) {
            if (isTrue(extractValue(soknad.getFaktumMedKeyOgParentFaktum(faktumKey, parentFaktumId), Boolean.class))) {
                AarsakTilBarnepass aarsakTilBarnepass = new AarsakTilBarnepass();
                String key = StofoKodeverkVerdier.BarnepassAarsak.valueOf(faktumKey.substring(faktumKey.lastIndexOf(".") + 1)).cmsKey;
                String cms = navMessageSource.getMessage(key, null, key, new Locale("nb", "NO"));
                aarsakTilBarnepass.setValue(cms);
                result.add(aarsakTilBarnepass);
            }
        }
        return result;
    }
}
