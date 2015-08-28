package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.BarnUnderAtten;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;

public class ReiseOppstartOgAvsluttetAktivitetTilXml implements Transformer<WebSoknad, ReiseOppstartOgAvsluttetAktivitet> {

    private static final String FAKTUM_REISEMAAL = "reise.midlertidig.reisemaal";
    private static final String FAKTUM_PERIODE = "reise.midlertidig.periode";
    private static final String FAKTUM_AVSTAND = "reise.midlertidig.dagligreiseavstand";
    private static final String FAKTUM_HJEMMEBOENDE = "reise.midlertidig.hjemmeboende";

    @Override
    public ReiseOppstartOgAvsluttetAktivitet transform(WebSoknad soknad) {
        ReiseOppstartOgAvsluttetAktivitet reise = new ReiseOppstartOgAvsluttetAktivitet();
        reise.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey(FAKTUM_PERIODE)));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey(FAKTUM_AVSTAND), BigInteger.class));
        reise.setAktivitetsstedAdresse(extractValue(soknad.getFaktumMedKey(FAKTUM_REISEMAAL), StofoKodeverkVerdier.SammensattAdresse.class).sammensattAdresse);
        reise.setHarBarnUnderFemteklasse(extractValue(soknad.getFaktumMedKey(FAKTUM_HJEMMEBOENDE), Boolean.class));
        reise.setBarnUnderAtten(barnUnder18(soknad));
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "midlertidig"));
        return reise;
    }

    private BarnUnderAtten barnUnder18(WebSoknad soknad) {
        //TODO: Barn under 18 bør være liste. Støtte heller ikke dato.
        return StofoTransformers.extractValue(soknad.getFaktumMedKey("barn"), BarnUnderAtten.class, "fno");
    }


}
