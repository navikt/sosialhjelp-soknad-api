package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilBoolean;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class UtgifterGjeldTilXml implements Function<WebSoknad, XMLUtgifterGjeld> {

    @Override
    public XMLUtgifterGjeld apply(WebSoknad webSoknad) {
        XMLUtgifterGjeld utgifter = new XMLUtgifterGjeld();

        String harBoutgifterVerdi = webSoknad.getValueForFaktum("utgifter.boutgift");
//        utgifter.withBoutgifter(SoknadSosialhjelpUtils.tilBoolean(Boolean.valueOf(harBoutgifterVerdi)));

        if (harBoutgifterVerdi.equals("true")) {
//            utgifter
//                    .withUtgiftHusleie(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "utgifter.boutgift.true.type.husleie"))
//                    .withUtgiftStrom(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "utgifter.boutgift.true.type.strom"))
//                    .withUtgiftKommunaleAvgifter(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "utgifter.boutgift.true.type.kommunaleutgifter"))
//                    .withUtgiftOppvarming(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "utgifter.boutgift.true.type.oppvarming"))
//                    .withUtgiftAvdragRenterBoliglaan(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "utgifter.boutgift.true.type.avdraglaan"));

            Boolean andreUtgifterVerdi = Boolean.valueOf(webSoknad.getValueForFaktum("utgifter.boutgift.true.type.andreutgifter"));
//            utgifter.withAndreUtgifter(SoknadSosialhjelpUtils.tilBoolean(andreUtgifterVerdi));

            if (andreUtgifterVerdi) {
//                utgifter.withBeskrivelseAndreUtgifter(tilXMLKildeString(webSoknad, "utgifter.boutgift.true.type.andreutgifter.true.beskrivelse"));
            }
        }

        return utgifter;
    }
}