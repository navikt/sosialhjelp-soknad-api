package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseObligatoriskSamling;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;

public class SamlingReiseTilXml {
    public ReiseObligatoriskSamling transform(WebSoknad soknad) {
        ReiseObligatoriskSamling reise = new ReiseObligatoriskSamling();
        reise.setPeriode(reiseSamlinger(soknad));
        reise.setReiseadresser(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey("reise.samling.reisemaal")));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.samling.reiselengde"), BigInteger.class)); //TODO har desimaler
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "samling")); //TODO - kan ikke gjenbruke alternativTransportUtgifter da denne skal ha utgifter per samling
        //TODO legge til samlingsdato når denne blir fikset

        return reise;
    }

    private Periode reiseSamlinger(WebSoknad soknad) {
        String samlingsfaktum = soknad.getFaktumMedKey("reise.samling.fleresamlinger").getValue();
        if(samlingsfaktum != null && samlingsfaktum.equals("en")) {
            return faktumTilPeriode(soknad.getFaktumMedKey("reise.samling.aktivitetsperiode"));
        }
        return faktumTilPeriode(lagPeriodeAvForsteOgSisteSamlingsperiode(soknad));
    }

    private Faktum lagPeriodeAvForsteOgSisteSamlingsperiode(WebSoknad soknad) {
        List<Faktum> periodeFakta = soknad.getFaktaMedKey("reise.samling.fleresamlinger.samling");
        String fom = periodeFakta.get(0).getProperties().get("fom");
        String tom = periodeFakta.get(0).getProperties().get("tom");

        for (Faktum faktum : periodeFakta) {
            if(faktum.getProperties().get("fom").compareTo(fom) < 0) {
                fom = faktum.getProperties().get("fom");
            }
            if(faktum.getProperties().get("tom").compareTo(tom) > 0) {
                tom = faktum.getProperties().get("tom");
            }
        }

        return new Faktum().medProperty("fom", fom).medProperty("tom", tom);
    }
}
