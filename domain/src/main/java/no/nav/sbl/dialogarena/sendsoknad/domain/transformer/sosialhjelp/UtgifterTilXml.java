package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLBoolean;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLString;


public class UtgifterTilXml implements Function<WebSoknad, XMLUtgifter> {

    @Override
    public XMLUtgifter apply(WebSoknad webSoknad) {
        XMLUtgifter utgifter = new XMLUtgifter();

        String harBoutgifterVerdi = webSoknad.getValueForFaktum("utgifter.boutgift");
        utgifter.withBoutgifter(tilXMLBoolean(Boolean.valueOf(harBoutgifterVerdi)));

        if (harBoutgifterVerdi.equals("true")) {
            utgifter
                    .withUtgiftHusleie(tilXMLBoolean(webSoknad, "utgifter.boutgift.true.type.husleie"))
                    .withUtgiftStrom(tilXMLBoolean(webSoknad, "utgifter.boutgift.true.type.strom"))
                    .withUtgiftKommunaleAvgifter(tilXMLBoolean(webSoknad, "utgifter.boutgift.true.type.kommunaleutgifter"))
                    .withUtgiftOppvarming(tilXMLBoolean(webSoknad, "utgifter.boutgift.true.type.oppvarming"))
                    .withUtgiftAvdragRenterBoliglaan(tilXMLBoolean(webSoknad, "utgifter.boutgift.true.type.avdraglaan"));

            Boolean andreUtgifterVerdi = Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.true.type.andreutgifter"));
            utgifter.withAndreUtgifter(tilXMLBoolean(andreUtgifterVerdi));

            if (andreUtgifterVerdi) {
                utgifter.withBeskrivelseAndreUtgifter(tilXMLString(webSoknad, "utgifter.boutgift.true.type.andreutgifter.true.beskrivelse"));
            }
        }

        return utgifter;
    }
}