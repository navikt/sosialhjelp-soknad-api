package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.apache.commons.collections15.*;
import org.apache.commons.lang3.*;

import java.util.*;

import static no.nav.modig.lang.collections.IterUtils.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.TilsynForetasAv.TRANSFORMER;

public class TilsynFamilieTilXml implements Transformer<WebSoknad, TilsynsutgifterFamilie> {
    @Override
    public TilsynsutgifterFamilie transform(WebSoknad soknad) {
        TilsynsutgifterFamilie familie = new TilsynsutgifterFamilie();
        familie.setPeriode(extractValue(soknad.getFaktumMedKey("tilsynfamilie.periode"), Periode.class));
        familie.setTilsynsmottaker(extractValue(soknad.getFaktumMedKey("tilsynfamilie.persontilsyn"), String.class));
        familie.setTilsynForetasAv(foretasAv(soknad.getFaktumMedKey("tilsynfamilie.typetilsyn")));
        Boolean deletilsyn = extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), Boolean.class);
        familie.setDeltTilsyn(deletilsyn);
        if (deletilsyn != null && deletilsyn) {
            familie.setAnnenTilsynsperson(extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), String.class, "personnummer"));
        }

        return familie;
    }

    private String foretasAv(final Faktum faktum) {
        List<String> tilsyn = on(faktum.getProperties()).filter(propertyIsValue("true")).map(KEYS).map(TRANSFORMER).collect();
        return StringUtils.join(tilsyn, ",");
    }
}
