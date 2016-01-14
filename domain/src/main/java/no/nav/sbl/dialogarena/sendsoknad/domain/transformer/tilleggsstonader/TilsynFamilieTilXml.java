package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterFamilie;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.propertyIsValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;

public class TilsynFamilieTilXml implements Transformer<WebSoknad, TilsynsutgifterFamilie> {
    @Override
    public TilsynsutgifterFamilie transform(WebSoknad soknad) {
        TilsynsutgifterFamilie familie = new TilsynsutgifterFamilie();
        familie.setPeriode(extractValue(soknad.getFaktumMedKey("tilsynfamilie.periode"), Periode.class));
        familie.setTilsynsmottaker(extractValue(soknad.getFaktumMedKey("tilsynfamilie.persontilsyn"), String.class));
        familie.setTilsynForetasAv(foretasAv(soknad.getFaktumMedKey("tilsynfamilie.typetilsyn")));
        Boolean deletilsyn = extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), Boolean.class);
        familie.setDeltTilsyn(deletilsyn);
        if(deletilsyn != null && deletilsyn) {
            familie.setAnnenTilsynsperson(extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), String.class, "personnummer"));
        }

        return familie;
    }

    private String foretasAv(final Faktum faktum) {
        List<String> tilsyn = on(faktum.getProperties()).filter(propertyIsValue("true")).map(FaktumPredicates.KEYS).map(StofoKodeverkVerdier.TilsynForetasAv.TRANSFORMER).collect();
        return StringUtils.join(tilsyn, ",");
    }
}
