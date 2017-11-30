package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld.XMLBarnutgifter;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld.XMLBarnutgifter.XMLBarnutgifterliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld.XMLBoutgifter;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLUtgifterGjeld.XMLBoutgifter.XMLBoutgifterliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBarnutgift;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBoutgift;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBarnutgift.*;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBoutgift.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.lagListeFraFakta;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class UtgifterGjeldTilXml implements Function<WebSoknad, XMLUtgifterGjeld> {

    private static final Map<String, String> BOUTGIFT_MAP = lagBoutgiftMap();
    private static final Map<String, String> BARNEUTGIFT_MAP = lagBarneutgiftMap();

    private static Map<String, String> lagBoutgiftMap() {
        Map<String, String> map = new HashMap<>();
        map.put("utgifter.boutgift.true.type.husleie", HUSLEIE.value());
        map.put("utgifter.boutgift.true.type.strom", STROM.value());
        map.put("utgifter.boutgift.true.type.kommunaleavgifter", KOMMUNALEAVGIFTER.value());
        map.put("utgifter.boutgift.true.type.oppvarming", OPPVARMING.value());
        map.put("utgifter.boutgift.true.type.avdraglaan", BOLIGLAN.value());
        map.put("utgifter.boutgift.true.type.andreutgifter", XMLBoutgift.ANNET.value());
        return map;
    }

    private static Map<String, String> lagBarneutgiftMap() {
        Map<String, String> map = new HashMap<>();
        map.put("utgifter.barn.true.utgifter.fritidsaktivitet", FRITIDSAKTIVITETER.value());
        map.put("utgifter.barn.true.utgifter.barnehage", BARNEHAGE.value());
        map.put("utgifter.barn.true.utgifter.tannbehandling", TANNBEHANDLING.value());
        map.put("utgifter.barn.true.utgifter.annet", XMLBarnutgift.ANNET.value());
        return map;
    }

    @Override
    public XMLUtgifterGjeld apply(WebSoknad webSoknad) {
        return new XMLUtgifterGjeld()
                .withBoutgifter(new XMLBoutgifter()
                        .withHarBoutgifter(tilString(webSoknad, "utgifter.boutgift"))
                        .withBoutgifterliste(new XMLBoutgifterliste(lagListeFraFakta(webSoknad, BOUTGIFT_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "utgifter.boutgift.true.type.andreutgifter.true.beskrivelse")))
                .withBarnutgifter(new XMLBarnutgifter()
                        .withHarBarneutgifter(tilString(webSoknad, "utgifter.barn"))
                        .withBarnutgifterliste(new XMLBarnutgifterliste(lagListeFraFakta(webSoknad, BARNEUTGIFT_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "utgifter.barn.true.utgifter.annet.true.beskrivelse")));
    }
}