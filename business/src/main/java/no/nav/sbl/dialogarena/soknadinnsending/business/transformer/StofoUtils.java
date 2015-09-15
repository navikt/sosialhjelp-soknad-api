package no.nav.sbl.dialogarena.soknadinnsending.business.transformer;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.AlternativeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier;

import static java.lang.String.format;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.StofoTransformers.extractValue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class StofoUtils {

    private static final String FAKTUM_DROSJE_BELOP = "reise.%s.offentligtransport.drosje.belop";
    private static final String FAKTUM_EGENBIL = "reise.%s.offentligtransport.egenbil";
    private static final String FAKTUM_OFFENTLIGTRANSPORT_BOMPENGER = "reise.%s.offentligtransport.egenbil.kostnader.bompenger";
    private static final String FAKTUM_OFFENTLIGTRANSPORT_PARKERING = "reise.%s.offentligtransport.egenbil.kostnader.parkering";
    private static final String FAKTUM_OFFENTLIGTRANSPORT_PIGGDEKK = "reise.%s.offentligtransport.egenbil.kostnader.piggdekk";
    private static final String FAKTUM_OFFENTLIGTRANSPORT_FERGE = "reise.%s.offentligtransport.egenbil.kostnader.ferge";
    private static final String FAKTUM_OFFENTLIGTRANSPORT_ANNET = "reise.%s.offentligtransport.egenbil.kostnader.annet";

    public static AlternativeTransportutgifter alternativeTransportUtgifter(WebSoknad soknad, String bolk) {
        AlternativeTransportutgifter utgifter = new AlternativeTransportutgifter();
        utgifter.setDrosjeTransportutgifter(extractValue(soknad.getFaktumMedKey(format(FAKTUM_DROSJE_BELOP, bolk)), DrosjeTransportutgifter.class));

        utgifter.setKanEgenBilBrukes(extractValue(soknad.getFaktumMedKey(format(FAKTUM_EGENBIL, bolk)), Boolean.class));
        if (isTrue(utgifter.isKanEgenBilBrukes())) {
            utgifter.setEgenBilTransportutgifter(egenBilUtgifter(soknad, bolk));
        }

        utgifter.setKanOffentligTransportBrukes(extractValue(soknad.getFaktumMedKey(format("reise.%s.offentligtransport", bolk)), Boolean.class));
        if(isTrue(utgifter.isKanOffentligTransportBrukes())) {
            utgifter.setKollektivTransportutgifter(extractValue(soknad.getFaktumMedKey(format("reise.%s.offentligtransport.utgift", bolk)), KollektivTransportutgifter.class));
        }
        return utgifter;
    }

    private static EgenBilTransportutgifter egenBilUtgifter(WebSoknad soknad, String bolk) {
        EgenBilTransportutgifter utgifter = new EgenBilTransportutgifter();
        Faktum[] fakta = new Faktum[]{
                soknad.getFaktumMedKey(format(FAKTUM_OFFENTLIGTRANSPORT_BOMPENGER, bolk)),
                soknad.getFaktumMedKey(format(FAKTUM_OFFENTLIGTRANSPORT_PARKERING, bolk)),
                soknad.getFaktumMedKey(format(FAKTUM_OFFENTLIGTRANSPORT_PIGGDEKK, bolk)),
                soknad.getFaktumMedKey(format(FAKTUM_OFFENTLIGTRANSPORT_FERGE, bolk)),
                soknad.getFaktumMedKey(format(FAKTUM_OFFENTLIGTRANSPORT_ANNET, bolk))
        };
        utgifter.setSumAndreUtgifter(StofoTransformers.sumDouble(fakta));
        return utgifter;
    }

    public static String sammensattAdresse(Faktum faktum) {
        StofoKodeverkVerdier.SammensattAdresse sammensattAdresse = extractValue(faktum, StofoKodeverkVerdier.SammensattAdresse.class);
        return sammensattAdresse != null ? sammensattAdresse.sammensattAdresse : null;
    }
}
