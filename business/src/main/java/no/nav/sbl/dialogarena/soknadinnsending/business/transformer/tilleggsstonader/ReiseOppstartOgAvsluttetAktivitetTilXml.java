package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.AlternativeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.BarnUnderAtten;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DagligReise;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Formaal;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;

public class ReiseOppstartOgAvsluttetAktivitetTilXml implements Transformer<WebSoknad, ReiseOppstartOgAvsluttetAktivitet> {
    @Override
    public ReiseOppstartOgAvsluttetAktivitet transform(WebSoknad soknad) {
        ReiseOppstartOgAvsluttetAktivitet reise = new ReiseOppstartOgAvsluttetAktivitet();
        reise.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey("reise.midlertidig.periode")));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.midlertidig.dagligreiseavstand"), BigInteger.class));
        reise.setAktivitetsstedAdresse(aktivitetsAdresse(soknad));
        reise.setHarBarnUnderFemteklasse(extractValue(soknad.getFaktumMedKey("reise.midlertidig.hjemmeboende"), Boolean.class));
        reise.setBarnUnderAtten(barnUnder18(soknad));
        reise.setAlternativeTransportutgifter(alternativeTransportUtgifter(soknad));
        return reise;
    }

    private BarnUnderAtten barnUnder18(WebSoknad soknad) {
        //TODO: Barn under 18 bør være liste. Støtte heller ikke dato.
        return StofoTransformers.extractValue(soknad.getFaktumMedKey("barn"), BarnUnderAtten.class, "fno");
    }

    private String aktivitetsAdresse(WebSoknad soknad) {
        return String.format("%s, %s",
                extractValue(soknad.getFaktumMedKey("reise.midlertidig.reisemaal"), String.class, "adresse"),
                extractValue(soknad.getFaktumMedKey("reise.midlertidig.reisemaal"), String.class, "postnr"));
    }

    private AlternativeTransportutgifter alternativeTransportUtgifter(WebSoknad soknad) {
        AlternativeTransportutgifter utgifter = new AlternativeTransportutgifter();
        utgifter.setDrosjeTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.drosje.belop"), DrosjeTransportutgifter.class));

        utgifter.setKanEgenBilBrukes(extractValue(soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil"), Boolean.class));
        if(utgifter.isKanEgenBilBrukes()) {
            utgifter.setEgenBilTransportutgifter(egenBilUtgifter(soknad));
        }

        utgifter.setKanOffentligTransportBrukes(extractValue(soknad.getFaktumMedKey("reise.midlertidig.offentligtransport"), Boolean.class));
        utgifter.setKollektivTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.utgift"), KollektivTransportutgifter.class));
        return utgifter;
    }

    private EgenBilTransportutgifter egenBilUtgifter(WebSoknad soknad) {
        EgenBilTransportutgifter utgifter = new EgenBilTransportutgifter();
        utgifter.setSumAndreUtgifter(
                StofoTransformers.sumDouble(
                        soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil.kostnader.bompenger"),
                        soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil.kostnader.parkering"),
                        soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil.kostnader.piggdekk"),
                        soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil.kostnader.ferge"),
                        soknad.getFaktumMedKey("reise.midlertidig.offentligtransport.egenbil.kostnader.annet")
                ));
        return utgifter;
    }
}
