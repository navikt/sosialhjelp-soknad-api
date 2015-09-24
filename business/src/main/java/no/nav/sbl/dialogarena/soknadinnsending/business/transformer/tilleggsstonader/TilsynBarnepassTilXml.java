package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilsynskategorier;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.context.MessageSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.FaktumPredicates.harValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers.extractValue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class TilsynBarnepassTilXml extends CmsTransformer<WebSoknad, TilsynsutgifterBarn> {
    public static final String BARNEPASS_ANDREFORELDER = "barnepass.andreforelder";
    public static final String BARNEPASS_TYPER = "barnepass.typer";
    public static final String BARNEPASS_FOLLFORT_FJERDE = "barnepass.fjerdeklasse";
    public static final List<String> BARNEPASS_AARSAKER = Arrays.asList("barnepass.fjerdeklasse.langvarig", "barnepass.fjerdeklasse.trengertilsyn", "barnepass.fjerdeklasse.ingen");
    private static final String PERIODE = "barnepass.periode";
    private static final String UTBETALINGSDATO = "barnepass.utbetalingsdato";
    private static final String SOKERBARNEPASS = "barnepass.sokerbarnepass";
    private TilsynsutgifterBarn tilsynsutgifterBarn = new TilsynsutgifterBarn();

    public TilsynBarnepassTilXml(MessageSource navMessageSource) {
        super(navMessageSource);
    }

    @Override
    public TilsynsutgifterBarn transform(WebSoknad soknad) {
        tilsynsutgifterBarn.setPeriode(extractValue(soknad.getFaktumMedKey(PERIODE), Periode.class));
        barnSomDetSokesBarnepassOm(soknad);
        tilsynsutgifterBarn.setOensketUtbetalingsdag(extractValue(soknad.getFaktumMedKey(UTBETALINGSDATO), BigInteger.class));
        tilsynsutgifterBarn.setAnnenForsoergerperson(extractValue(soknad.getFaktumMedKey(BARNEPASS_ANDREFORELDER), String.class));

        return tilsynsutgifterBarn;
    }

    private void barnSomDetSokesBarnepassOm(WebSoknad soknad) {
        List<Faktum> sokerBarnepassBarn = on(soknad.getFaktaMedKey(SOKERBARNEPASS)).filter(harValue("true")).collect();
        for (Faktum barnepass : sokerBarnepassBarn) {
            Faktum barn = soknad.finnFaktum(Long.valueOf(barnepass.getProperties().get("tilknyttetbarn")));
            if (barn != null) {
                Barn stofoBarn = extractValue(barn, Barn.class);

                Faktum barnepassType = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER, barnepass.getFaktumId());
                stofoBarn.setTilsynskategori(extractValue(barnepassType, Tilsynskategorier.class));

                Faktum fulfortFjerde = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_FOLLFORT_FJERDE, barnepass.getFaktumId());
                stofoBarn.setHarFullfoertFjerdeSkoleaar(extractValue(fulfortFjerde, Boolean.class));

                List<String> aarsakTilBarnepasses = aarsaker(soknad, fulfortFjerde.getFaktumId());
                stofoBarn.getAarsakTilBarnepass().addAll(aarsakTilBarnepasses);
                tilsynsutgifterBarn.getBarn().add(stofoBarn);


            }
        }
    }

    private List<String> aarsaker(WebSoknad soknad, Long parentFaktumId) {
        List<String> result = new ArrayList<>();
        for (String faktumKey : BARNEPASS_AARSAKER) {
            if (isTrue(extractValue(soknad.getFaktumMedKeyOgParentFaktum(faktumKey, parentFaktumId), Boolean.class))) {
                String key = StofoKodeverkVerdier.BarnepassAarsak.valueOf(faktumKey.substring(faktumKey.lastIndexOf(".") + 1)).cmsKey;
                result.add(cms(key));
            }
        }
        return result;
    }
}
