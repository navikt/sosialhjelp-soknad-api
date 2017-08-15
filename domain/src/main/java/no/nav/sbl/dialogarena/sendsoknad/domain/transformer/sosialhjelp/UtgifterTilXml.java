package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;


public class UtgifterTilXml implements Function<WebSoknad, XMLUtgifter> {

    @Override
    public XMLUtgifter apply(WebSoknad webSoknad) {
        XMLUtgifter utgifter = new XMLUtgifter();

        String harBoutgifterVerdi = webSoknad.getValueForFaktum("utgifter.boutgift");
        utgifter.withBoutgifter(new XMLBoolean()
                .withValue(Boolean.valueOf(harBoutgifterVerdi))
                .withKilde(BRUKER));

        if (harBoutgifterVerdi.equals("false")) {
            utgifter.withUtgiftHusleie(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.husleie"))));

            utgifter.withUtgiftStrom(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.strom"))));

            utgifter.withUtgiftKommunaleAvgifter(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.kommunaleutgifter"))));

            utgifter.withUtgiftOppvarming(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.oppvarming"))));

            utgifter.withUtgiftAvdragRenterBoliglaan(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.avdraglaan"))));

            Boolean andreUtgifterVerdi = Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.andreutgifter"));
            utgifter.withAndreUtgifter(new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(andreUtgifterVerdi));

            if (andreUtgifterVerdi) {
                utgifter.withBeskrivelseAndreUtgifter(new XMLString()
                        .withKilde(BRUKER)
                        .withValue(webSoknad.getValueForFaktum("utgifter.boutgift.false.type.andreutgifter.true.beskrivelse")));
            }
        }

        return utgifter;
    }
}