package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;

public class ReiseOppstartOgAvsluttetAktivitetTilXml implements Transformer<WebSoknad, ReiseOppstartOgAvsluttetAktivitet> {

    private static final String FAKTUM_REISEMAAL = "reise.midlertidig.reisemaal";
    private static final String FAKTUM_PERIODE = "reise.midlertidig.periode";
    private static final String FAKTUM_AVSTAND = "reise.midlertidig.reiselengde";
    private static final String FAKTUM_HJEMMEBOENDE = "reise.midlertidig.hjemmeboende";

    @Override
    public ReiseOppstartOgAvsluttetAktivitet transform(WebSoknad soknad) {
        ReiseOppstartOgAvsluttetAktivitet reise = new ReiseOppstartOgAvsluttetAktivitet();
        reise.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey(FAKTUM_PERIODE)));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey(FAKTUM_AVSTAND), BigInteger.class));
        reise.setAktivitetsstedAdresse(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey(FAKTUM_REISEMAAL)));
        reise.setHarBarnUnderFemteklasse(extractValue(soknad.getFaktumMedKey(FAKTUM_HJEMMEBOENDE), Boolean.class));
        reise.setHarBarnUnderAtten(harBarnUnder18(soknad));
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "midlertidig"));
        return reise;
    }

    private Boolean harBarnUnder18(WebSoknad soknad) {
        for (Faktum faktum : soknad.getFaktaMedKey("barn")) {
            if (faktum.harPropertySomMatcher("skalFlytteMed", "true")) {
                return true;
            }
        }
        return false;
    }

}
