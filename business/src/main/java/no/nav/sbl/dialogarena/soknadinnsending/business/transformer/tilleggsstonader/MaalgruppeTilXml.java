package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppeinformasjon;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppetyper;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers;
import org.apache.commons.collections15.Transformer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MaalgruppeTilXml implements Transformer<Faktum, Maalgruppeinformasjon> {

    private static final List<String> MAALGRUPPER_SOM_IKKE_SKAL_TIL_ARENA = Arrays.asList("", "annet");

    @Override
    public Maalgruppeinformasjon transform(Faktum faktum) {
        Map<String, String> properties = faktum.getProperties();

        Maalgruppeinformasjon informasjon = new Maalgruppeinformasjon();
        informasjon.setPeriode(StofoTransformers.extractValue(faktum, Periode.class));
        informasjon.setMaalgruppetype(lagType(properties));
        informasjon.setKilde(faktum.getType().toString());
        if(MAALGRUPPER_SOM_IKKE_SKAL_TIL_ARENA.contains(informasjon.getMaalgruppetype().getValue().toLowerCase())){
            return null;
        }
        return informasjon;
    }

    private Maalgruppetyper lagType(Map<String, String> properties) {
        Maalgruppetyper type = new Maalgruppetyper();
        type.setKodeverksRef(properties.get("kodeverkVerdi"));
        type.setValue(properties.get("kodeverkVerdi"));
        return type;
    }
}
