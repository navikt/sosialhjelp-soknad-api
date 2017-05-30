package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.TilknytningNorge;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Utenlandsopphold;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class TilknytningTilXml implements Function<WebSoknad, TilknytningNorge> {
    @Override
    public TilknytningNorge apply(WebSoknad webSoknad) {
        TilknytningNorge tilknytningNorge = new TilknytningNorge();

        Boolean tidligere = Boolean.valueOf(webSoknad.getFaktumMedKey("tilknytningnorge.tidligere").getValue());

        tilknytningNorge.setTidligereOppholdNorge(tidligere);
        if (!tidligere) {
            tilknytningNorge.withTidligereOppholdUtenlands(new Utenlandsopphold());
        }

        return tilknytningNorge
                .withOppholdNorgeNaa(Boolean.valueOf(webSoknad.getFaktumMedKey("tilknytningnorge.oppholder").getValue()));
    }
}
