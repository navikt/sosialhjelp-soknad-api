package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ErUtgifterDekket;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Laeremiddelutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Skolenivaaer;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.faktumTilPeriode;

public class LaeremidlerTilXml implements Transformer<WebSoknad, Laeremiddelutgifter> {
    @Override
    public Laeremiddelutgifter transform(WebSoknad soknad) {
        Laeremiddelutgifter laeremiddel = new Laeremiddelutgifter();

        Faktum funksjonshemmingFaktum = soknad.getFaktumMedKey("laeremidler.funksjonshemming");
        if(funksjonshemmingFaktum != null && "true".equals(funksjonshemmingFaktum.getValue())){
            laeremiddel.setBeloep(extractValue(funksjonshemmingFaktum, BigInteger.class, "utgift"));
        }

        laeremiddel.setErUtgifterDekket(extractValue(soknad.getFaktumMedKey("laeremidler.dekket"), ErUtgifterDekket.class));
        laeremiddel.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey("laeremidler.periode")));
        laeremiddel.setProsentandelForUtdanning(extractValue(soknad.getFaktumMedKey("laeremidler.deltidsstudent"), BigInteger.class));
        laeremiddel.setSkolenivaa(extractValue(soknad.getFaktumMedKey("laeremidler.utdanningstype"), Skolenivaaer.class));
        return laeremiddel;
    }
}
