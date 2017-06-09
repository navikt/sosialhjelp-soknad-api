package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;


import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Soknadsvalg;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Stoenadstype;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class SoknadsvalgTilXml implements Function<WebSoknad, Soknadsvalg> {

    public Soknadsvalg apply(WebSoknad webSoknad) {
        Soknadsvalg soknadsvalg = new Soknadsvalg();

        String stonadstype = webSoknad.getValueForFaktum("soknadsvalg.stonadstype");
        if (stonadstype.equals(Stonadstyper.ENGANGSSTONAD_FAR)) {
            soknadsvalg.withStoenadstype(Stoenadstype.ENGANGSSTOENADFAR);
        } else if (stonadstype.equals(Stonadstyper.ENGANGSSTONAD_MOR)) {
            soknadsvalg.withStoenadstype(Stoenadstype.ENGANGSSTOENADMOR);
        }

        String fodselEllerAdopsjon = webSoknad.getValueForFaktum("soknadsvalg.fodselelleradopsjon");
        if (fodselEllerAdopsjon.equals("fodsel")) {
            soknadsvalg.withFoedselEllerAdopsjon(FoedselEllerAdopsjon.FOEDSEL);
        } else if (fodselEllerAdopsjon.equals("adopsjon")) {
            soknadsvalg.withFoedselEllerAdopsjon(FoedselEllerAdopsjon.ADOPSJON);
        }

        return soknadsvalg;
    }
}
