package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.GrunnlagForAnsvarsovertakelse;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Rettigheter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class RettigheterTilXml implements Function<WebSoknad, Rettigheter> {

    @Override
    public Rettigheter apply(WebSoknad webSoknad) {
        try {
            String overtak = webSoknad.getValueForFaktum("rettigheter.overtak");
            GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse = GrunnlagForAnsvarsovertakelse.fromValue(overtak);
            return new Rettigheter().withGrunnlagForAnsvarsovertakelse(grunnlagForAnsvarsovertakelse);
        } catch (IllegalArgumentException e) {
            return new Rettigheter();
        }
    }

}
