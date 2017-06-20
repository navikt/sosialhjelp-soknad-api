package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Aktoer;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class BrukerTilXml implements Function<WebSoknad, Aktoer> {

    @Override
    public Aktoer apply(WebSoknad webSoknad) {
        String fodselsnummer = webSoknad.getFodselsnummer();

        return new Bruker().withPersonidentifikator(fodselsnummer);
    }

}
