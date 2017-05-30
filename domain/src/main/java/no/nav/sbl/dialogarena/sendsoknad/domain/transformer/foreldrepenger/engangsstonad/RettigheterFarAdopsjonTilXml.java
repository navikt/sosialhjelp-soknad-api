package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Rettigheter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class RettigheterFarAdopsjonTilXml implements Function<WebSoknad, Rettigheter> {

    @Override
    public Rettigheter apply(WebSoknad webSoknad) {
        return new Rettigheter();
    }

    /*
    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform();
    }
    */

}
