package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

class SoknadSosialhjelpUtils {

    static XMLBoolean tilXMLBoolean(WebSoknad webSoknad, String faktum) {
        return tilXMLBoolean(webSoknad, faktum, BRUKER);
    }

    static XMLBoolean tilXMLBoolean(WebSoknad webSoknad, String faktum, XMLKilde kilde) {
        return tilXMLBoolean(Boolean.valueOf(webSoknad.getValueForFaktum(faktum)), kilde);
    }

    static XMLBoolean tilXMLBoolean(Boolean verdi) {
        return tilXMLBoolean(verdi, BRUKER);
    }

    static XMLBoolean tilXMLBoolean(Boolean verdi, XMLKilde kilde) {
        return new XMLBoolean().withValue(verdi).withKilde(kilde);
    }


    static XMLString tilXMLString(WebSoknad webSoknad, String faktum) {
        return tilXMLString(webSoknad, faktum, BRUKER);
    }

    static XMLString tilXMLString(WebSoknad webSoknad, String faktum, XMLKilde kilde) {
        return tilXMLString(webSoknad.getValueForFaktum(faktum), kilde);
    }

    static XMLString tilXMLString(String verdi, XMLKilde kilde) {
        return  new XMLString()
                .withKilde(kilde)
                .withValue(verdi);
    }
}
