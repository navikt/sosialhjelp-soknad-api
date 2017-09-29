package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKildeString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

class SoknadSosialhjelpUtils {

    static XMLKildeString tilString(String verdi) {
        return tilString(verdi, BRUKER);
    }

    static XMLKildeString tilString(WebSoknad webSoknad, String faktum) {
        return tilString(webSoknad, faktum, BRUKER);
    }

    static XMLKildeString tilString(WebSoknad webSoknad, String faktum, XMLKilde kilde) {
        return tilString(webSoknad.getValueForFaktum(faktum), kilde);
    }

    static XMLKildeString tilString(String verdi, XMLKilde kilde) {
        return  new XMLKildeString()
                .withKilde(kilde.value())
                .withValue(verdi);
    }

    static List<XMLKildeString> lagListeFraFakta(WebSoknad webSoknad, Map<String, String> faktumKeyTilEnumVerdi) {
        return faktumKeyTilEnumVerdi.entrySet().stream()
                .filter(e -> webSoknad.getValueForFaktum(e.getKey()).equals("true"))
                .map(e -> tilString(e.getValue()))
                .collect(Collectors.toList());
    }
}
