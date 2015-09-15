package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;


import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DagligReise;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.faktumTilPeriode;

public class DagligReiseTilXml implements Transformer<WebSoknad, DagligReise> {
    @Override
    public DagligReise transform(WebSoknad soknad) {
        DagligReise reise = new DagligReise();
        reise.setAktivitetsadresse(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey("reise.aktivitet.reisemaal")));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.aktivitet.dagligreiseavstand"), Double.class));
        reise.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey("reise.aktivitet.periode")));
        Faktum harParkering = soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.parkering");
        if (harParkering != null) {
            reise.setHarParkeringsutgift(extractValue(harParkering, Boolean.class));
            reise.setParkeringsutgiftBeloep(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.parkering.belop"), BigInteger.class));
        }
        reise.setInnsendingsintervall(extractValue(soknad.getFaktumMedKey("reise.aktivitet.offentligtransport.egenbil.sendebekreftelse"), Innsendingsintervaller.class));
        reise.setOensketUtbetalingsdag(extractValue(soknad.getFaktumMedKey("reise.aktivitet.utbetalingsdato"), BigInteger.class));
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "aktivitet"));
        reise.setHarMedisinskeAarsakerTilTransport(extractValue(soknad.getFaktumMedKey("reise.aktivitet.medisinskeaarsaker"), Boolean.class));
        return reise;
    }

}
