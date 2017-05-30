package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;

public class RettigheterFarAdopsjonTilXml implements AlternativRepresentasjonTransformer {

    public AlternativRepresentasjon transform(Faktum faktum) {

        return new AlternativRepresentasjon();
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    /*
    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform();
    }
    */

}
