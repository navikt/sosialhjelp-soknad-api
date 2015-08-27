package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;


import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.AlternativeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Formaal;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReisestoenadForArbeidssoeker;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;

public class ArbeidReiseTilXml implements Transformer<WebSoknad, ReisestoenadForArbeidssoeker> {
    @Override
    public ReisestoenadForArbeidssoeker transform(WebSoknad soknad) {
        ReisestoenadForArbeidssoeker reise = new ReisestoenadForArbeidssoeker();
        reise.setReisedato(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.registrert"), XMLGregorianCalendar.class));
        reise.setFormaal(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.hvorforreise"), Formaal.class));
        reise.setAdresse(aktivitetsAdresse(soknad));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reiselengde"), BigInteger.class));
        reise.setErUtgifterDekketAvAndre(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reisedekket"), Boolean.class));
        reise.setErVentetidForlenget(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.dagpenger.forlenget"), Boolean.class));
        reise.setFinnesTidsbegrensetbortfall(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.dagpenger.bortfall"), Boolean.class));
        reise.setAlternativeTransportutgifter(alternativeTransportUtgifter(soknad));
        return reise;
    }

    private String aktivitetsAdresse(WebSoknad soknad) {
        return String.format("%s, %s",
                extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reisemaal"), String.class, "adresse"),
                extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reisemaal"), String.class, "postnr"));
    }

    private AlternativeTransportutgifter alternativeTransportUtgifter(WebSoknad soknad) {
        AlternativeTransportutgifter utgifter = new AlternativeTransportutgifter();
        utgifter.setDrosjeTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.drosje.belop"), DrosjeTransportutgifter.class));

        utgifter.setKanEgenBilBrukes(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil"), Boolean.class));
        if(utgifter.isKanEgenBilBrukes() != null && utgifter.isKanEgenBilBrukes()) {
            utgifter.setEgenBilTransportutgifter(egenBilUtgifter(soknad));
        }

        utgifter.setKanOffentligTransportBrukes(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport"), Boolean.class));
        utgifter.setKollektivTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.utgift"), KollektivTransportutgifter.class));
        return utgifter;
    }

    private EgenBilTransportutgifter egenBilUtgifter(WebSoknad soknad) {
        EgenBilTransportutgifter utgifter = new EgenBilTransportutgifter();
        utgifter.setSumAndreUtgifter(
                StofoTransformers.sumDouble(
                        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.bompenger"),
                        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.parkering"),
                        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.piggdekk"),
                        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.ferge"),
                        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.annet")
                ));
        return utgifter;
    }

}
