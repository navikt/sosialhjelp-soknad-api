package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseObligatoriskSamling;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.sumDouble;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class SamlingReiseTilXml {
    public ReiseObligatoriskSamling transform(WebSoknad soknad) {
        ReiseObligatoriskSamling reise = new ReiseObligatoriskSamling();
        reise.setPeriode(reiseSamlinger(soknad));
        leggTilSamlinger(soknad, reise);

        reise.setReiseadresser(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey("reise.samling.reisemaal")));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.samling.reiselengde"), BigInteger.class)); //TODO har desimaler
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "samling"));
        setSpesielleUtgifterForSamlingsreise(soknad, reise);

        return reise;
    }

    private void setSpesielleUtgifterForSamlingsreise(WebSoknad soknad, ReiseObligatoriskSamling reise) {
        if (isTrue(reise.getAlternativeTransportutgifter().isKanOffentligTransportBrukes())) {
            String samlingsfaktum = soknad.getFaktumMedKey("reise.samling.fleresamlinger").getValue();
            if (samlingsfaktum != null && samlingsfaktum.equals("en")) {
                reise.getAlternativeTransportutgifter().setKollektivTransportutgifter(
                        extractValue(soknad.getFaktumMedKey("reise.samling.aktivitetsperiode"), KollektivTransportutgifter.class, "utgiftoffentligtransport")
                );
            } else {
                List<Faktum> samlingFakta = soknad.getFaktaMedKey("reise.samling.fleresamlinger.samling");
                Double kollektivUtgifter = sumDouble("utgiftoffentligtransport", samlingFakta.toArray(new Faktum[samlingFakta.size()]));
                reise.getAlternativeTransportutgifter().setKollektivTransportutgifter(
                        extractValue(new Faktum().medValue("" + kollektivUtgifter.longValue()), KollektivTransportutgifter.class)
                );
            }
        }
    }


    private Periode reiseSamlinger(WebSoknad soknad) {
        String samlingsfaktum = soknad.getFaktumMedKey("reise.samling.fleresamlinger").getValue();
        if (samlingsfaktum != null && samlingsfaktum.equals("en")) {
            return faktumTilPeriode(soknad.getFaktumMedKey("reise.samling.aktivitetsperiode"));
        }
        return faktumTilPeriode(lagPeriodeAvForsteOgSisteSamlingsperiode(soknad));
    }

    private void leggTilSamlinger(WebSoknad soknad, ReiseObligatoriskSamling reise) {
        String samlingsfaktum = soknad.getFaktumMedKey("reise.samling.fleresamlinger").getValue();
        if (samlingsfaktum != null && samlingsfaktum.equals("en")) {
            reise.getSamlingsperiode().add(reise.getPeriode());
        } else {
            List<Faktum> periodeFakta = soknad.getFaktaMedKey("reise.samling.fleresamlinger.samling");
            for (Faktum faktum : periodeFakta) {
                reise.getSamlingsperiode().add(faktumTilPeriode(faktum));
            }
        }
    }

    private Faktum lagPeriodeAvForsteOgSisteSamlingsperiode(WebSoknad soknad) {
        List<Faktum> periodeFakta = soknad.getFaktaMedKey("reise.samling.fleresamlinger.samling");
        String fom = periodeFakta.get(0).getProperties().get("fom");
        String tom = periodeFakta.get(0).getProperties().get("tom");

        for (Faktum faktum : periodeFakta) {
            if (faktum.getProperties().get("fom").compareTo(fom) < 0) {
                fom = faktum.getProperties().get("fom");
            }
            if (faktum.getProperties().get("tom").compareTo(tom) > 0) {
                tom = faktum.getProperties().get("tom");
            }
        }

        return new Faktum().medProperty("fom", fom).medProperty("tom", tom);
    }
}
