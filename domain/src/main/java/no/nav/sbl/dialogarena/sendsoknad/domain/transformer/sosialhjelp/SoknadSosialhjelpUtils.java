package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKildeBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKildeString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

class SoknadSosialhjelpUtils {

    static XMLKildeBoolean tilBoolean(WebSoknad webSoknad, String faktum) {
        return tilBoolean(webSoknad, faktum, BRUKER);
    }

    static XMLKildeBoolean tilBoolean(WebSoknad webSoknad, String faktum, XMLKilde kilde) {
        return tilBoolean(Boolean.valueOf(webSoknad.getValueForFaktum(faktum)), kilde);
    }

    static XMLKildeBoolean tilBoolean(Boolean verdi) {
        return tilBoolean(verdi, BRUKER);
    }

    static XMLKildeBoolean tilBoolean(Boolean verdi, XMLKilde kilde) {
        return new XMLKildeBoolean().withValue(verdi).withKilde(kilde.value());
    }


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
}
