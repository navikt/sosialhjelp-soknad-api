package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Aktoer;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.AktoerId;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Bruker;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class BrukerTilXml implements Function<WebSoknad, Aktoer> {

    @Override
    public Aktoer apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();

        return new Bruker().withPersonidentifikator(personnummer);
    }

}
