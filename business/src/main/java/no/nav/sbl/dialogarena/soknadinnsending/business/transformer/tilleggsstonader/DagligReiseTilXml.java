package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;


import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.AlternativeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DagligReise;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumToPeriode;

public class DagligReiseTilXml implements Transformer<WebSoknad, DagligReise> {
    @Override
    public DagligReise transform(WebSoknad soknad) {
        DagligReise reise = new DagligReise();
        reise.setAktivitetsadresse(aktivitetsAdresse(soknad));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.aktivitet.dagligreiseavstand"), Double.class));
        reise.setPeriode(faktumToPeriode(soknad.getFaktumMedKey("reise.aktivitet.periode")));
        Faktum harParkering = soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.parkering");
        if (harParkering != null) {
            reise.setHarParkeringsutgift(extractValue(harParkering, Boolean.class));
            reise.setParkeringsutgiftBeloep(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.parkering.belop"), BigInteger.class));
        }
        reise.setInnsendingsintervall(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.sendebekreftelse"), Innsendingsintervaller.class));
        reise.setOensketUtbetalingsdag(extractValue(soknad.getFaktumMedKey("reise.aktivitet.utbetalingsdato"), BigInteger.class));
        reise.setAlternativeTransportutgifter(alternativeTransportUtgifter(soknad));
        return reise;
    }

    private String aktivitetsAdresse(WebSoknad soknad) {
        return String.format("%s, %s",
                extractValue(soknad.getFaktumMedKey("reise.aktivitet.reisemaal"), String.class, "adresse"),
                extractValue(soknad.getFaktumMedKey("reise.aktivitet.reisemaal"), String.class, "postnr"));
    }

    private AlternativeTransportutgifter alternativeTransportUtgifter(WebSoknad soknad) {
        AlternativeTransportutgifter utgifter = new AlternativeTransportutgifter();
        utgifter.setDrosjeTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.drosje.belop"), DrosjeTransportutgifter.class));

        utgifter.setKanEgenBilBrukes(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil"), Boolean.class));
        if(utgifter.isKanEgenBilBrukes()) {
            utgifter.setEgenBilTransportutgifter(egenBilUtgifter(soknad));
        }

        utgifter.setKanOffentligTransportBrukes(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport"), Boolean.class));
        utgifter.setKollektivTransportutgifter(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.utgift"), KollektivTransportutgifter.class));
        return utgifter;
    }

    private EgenBilTransportutgifter egenBilUtgifter(WebSoknad soknad) {
        EgenBilTransportutgifter utgifter = new EgenBilTransportutgifter();
        utgifter.setSumAndreUtgifter(
                StofoTransformers.sumDouble(
                        soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.kostnader.bompenger"),
                        soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.kostnader.piggdekk"),
                        soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.kostnader.ferge"),
                        soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.kostnader.annet")
                ));
        return utgifter;
    }

}
